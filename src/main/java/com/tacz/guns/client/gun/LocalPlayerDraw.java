package com.tacz.guns.client.gun;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.internal.GunAnimationStateMachine;
import com.tacz.guns.client.input.ShootKey;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.duck.KeepingItemRenderer;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ClientMessagePlayerDrawGun;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class LocalPlayerDraw {
    private final LocalPlayerDataHolder data;
    private final LocalPlayer player;

    public LocalPlayerDraw(LocalPlayerDataHolder data, LocalPlayer player) {
        this.data = data;
        this.player = player;
    }

    public void draw(ItemStack lastItem) {
        // 重置各种参数
        this.resetData();

        // 获取各种数据
        ItemStack currentItem = player.getMainHandItem();
        long drawTime = System.currentTimeMillis() - data.clientDrawTimestamp;
        IGun currentGun = IGun.getIGunOrNull(currentItem);
        IGun lastGun = IGun.getIGunOrNull(lastItem);

        // 计算 draw 时长和 putAway 时长
        if (drawTime >= 0) {
            drawTime = getDrawTime(lastItem, lastGun, drawTime);
        }
        long putAwayTime = Math.abs(drawTime);

        // 发包通知服务器
        if (Minecraft.getInstance().gameMode != null) {
            Minecraft.getInstance().gameMode.ensureHasSentCarriedItem();
        }
        NetworkHandler.CHANNEL.sendToServer(new ClientMessagePlayerDrawGun());

        // 不处于收枪状态时才能收枪
        if (drawTime >= 0) {
            doPutAway(lastItem, lastGun, putAwayTime);
        }

        // 异步放映抬枪动画
        if (currentGun != null) {
            doDraw(currentGun, currentItem, putAwayTime);
        }
    }

    private void doDraw(IGun currentGun, ItemStack currentItem, long putAwayTime) {
        TimelessAPI.getClientGunIndex(currentGun.getGunId(currentItem)).ifPresent(gunIndex -> {
            GunAnimationStateMachine animationStateMachine = gunIndex.getAnimationStateMachine();
            if (animationStateMachine == null) {
                return;
            }
            if (data.drawFuture != null) {
                data.drawFuture.cancel(false);
            }
            data.drawFuture = LocalPlayerDataHolder.SCHEDULED_EXECUTOR_SERVICE.schedule(() -> {
                Minecraft.getInstance().submitAsync(() -> {
                    animationStateMachine.onGunDraw();
                    SoundPlayManager.stopPlayGunSound();
                    SoundPlayManager.playDrawSound(player, gunIndex);
                });
            }, putAwayTime, TimeUnit.MILLISECONDS);
        });
    }

    private void doPutAway(ItemStack lastItem, IGun lastGun, long putAwayTime) {
        if (lastGun == null) {
            return;
        }
        TimelessAPI.getClientGunIndex(lastGun.getGunId(lastItem)).ifPresent(gunIndex -> {
            // 播放收枪音效
            SoundPlayManager.stopPlayGunSound();
            SoundPlayManager.playPutAwaySound(player, gunIndex);
            // 播放收枪动画
            GunAnimationStateMachine animationStateMachine = gunIndex.getAnimationStateMachine();
            if (animationStateMachine != null) {
                animationStateMachine.onGunPutAway(putAwayTime / 1000F);
                // 保持枪械的渲染直到收枪动作完成
                ((KeepingItemRenderer) Minecraft.getInstance().getItemInHandRenderer()).keep(lastItem, putAwayTime);
            }
        });
    }

    private long getDrawTime(ItemStack lastItem, IGun lastGun, long drawTime) {
        if (lastGun != null) {
            // 如果不处于收枪状态，则需要加上收枪的时长
            Optional<CommonGunIndex> gunIndex = TimelessAPI.getCommonGunIndex(lastGun.getGunId(lastItem));
            float putAwayTime = gunIndex.map(index -> index.getGunData().getPutAwayTime()).orElse(0F);
            if (drawTime > putAwayTime * 1000) {
                drawTime = (long) (putAwayTime * 1000);
            }
            data.clientDrawTimestamp = System.currentTimeMillis() + drawTime;
        } else {
            drawTime = 0;
            data.clientDrawTimestamp = System.currentTimeMillis();
        }
        return drawTime;
    }

    private void resetData() {
        // 锁上状态锁
        data.lockState(operator -> operator.getSynDrawCoolDown() > 0);
        // 重置客户端的 shoot 时间戳
        data.isShootRecorded = true;
        data.clientShootTimestamp = -1;
        // 重置客户端瞄准状态
        data.clientIsAiming = false;
        data.clientAimingProgress = 0;
        LocalPlayerDataHolder.oldAimingProgress = 0;
        // 重置拉栓状态
        data.isBolting = false;
        // 更新切枪时间戳
        if (data.clientDrawTimestamp == -1) {
            data.clientDrawTimestamp = System.currentTimeMillis();
        }
        // 重置连发状态
        ShootKey.resetBurstState();
    }
}