package com.tacz.guns.client.animation.statemachine;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource_new.index.ClientGunIndex;
import com.tacz.guns.resource_new.pojo.data.gun.Bolt;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GunAnimationStateContext extends ItemAnimationStateContext {
    private ItemStack currentGunItem;
    private IGun iGun;
    private ClientGunIndex clientGunIndex;
    private float partialTicks;
    private float walkDistAnchor = 0f;

    private <T> Optional<T> processGunData(BiFunction<IGun, ClientGunIndex, T> processor) {
        if (iGun != null && clientGunIndex != null) {
            return Optional.ofNullable(processor.apply(iGun, clientGunIndex));
        }
        return Optional.empty();
    }

    private <T> Optional<T> processGunOperator(Function<IClientPlayerGunOperator, T> processor) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            return Optional.ofNullable(processor.apply(IClientPlayerGunOperator.fromLocalPlayer(player)));
        }
        return Optional.empty();
    }

    private <T> Optional<T> processPlayer(Function<Player, T> processor) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            return Optional.ofNullable(processor.apply(player));
        }
        return Optional.empty();
    }

    /**
     * 获取枪膛中是否有子弹。
     * @return 枪膛中是否有子弹。如果是开膛待击的枪械，则此方法返回 false。
     */
    public boolean hasBulletInBarrel() {
        return processGunData((iGun, gunIndex) -> {
            Bolt boltType = gunIndex.getGunData().getBolt();
            return boltType != Bolt.OPEN_BOLT && iGun.hasBulletInBarrel(currentGunItem);
        }).orElse(false);
    }

    /**
     * 获取弹匣中的备弹数。
     * @return 返回弹匣中的备弹数，不计算已在枪管中的弹药。
     */
    public int getAmmoCount() {
        return processGunData((iGun, gunIndex) -> iGun.getCurrentAmmoCount(currentGunItem)).orElse(0);
    }

    /**
     * 获取枪械弹匣的最大备弹数。
     * @return 返回枪械弹匣的最大备弹数，不计算已在枪管中的弹药。
     */
    public int getMaxAmmoCount() {
        return processGunData(
                (iGun, gunIndex) ->
                        AttachmentDataUtils.getAmmoCountWithAttachment(currentGunItem, gunIndex.getGunData())
        ).orElse(0);
    }

    /**
     * 获取枪械当前的开火模式。
     * @return FireMode 枚举的 ordinal 值
     */
    public int getFireMode() {
        return processGunData((iGun, gunIndex) -> iGun.getFireMode(currentGunItem).ordinal()).orElse(0);
    }

    /**
     * 获取持枪玩家的瞄准进度。
     * @return 持枪玩家的瞄准进度，取值范围：0 ~ 1。
     *         0 代表没有喵准，1 代表喵准完成。
     */
    public float getAimingProgress() {
        return processGunOperator(operator -> operator.getClientAimingProgress(partialTicks)).orElse(0f);
    }

    /**
     * 获取玩家的射击冷却。
     * @return 玩家的射击冷却，单位为毫秒(ms)。
     */
    public long getShootCoolDown() {
        return processGunOperator(IClientPlayerGunOperator::getClientShootCoolDown).orElse(0L);
    }

    /**
     * 获取玩家的按键输入是否为上。
     * @return 玩家的按键输入是否为上 (对应着移动中的前进按键，如 W)
     */
    public boolean isInputUp() {
        return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.up).orElse(false);
    }

    /**
     * 获取玩家的按键输入是否为下。
     * @return 玩家的按键输入是否为下 (对应着移动中的后退按键，如 S)
     */
    public boolean isInputDown() {
        return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.down).orElse(false);
    }

    /**
     * 获取玩家的按键输入是否为左。
     * @return 玩家的按键输入是否为左 (对应着移动中的左移按键，如 A)
     */
    public boolean isInputLeft() {
        return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.left).orElse(false);
    }

    /**
     * 获取玩家的按键输入是否为右。
     * @return 玩家的按键输入是否为右 (对应着移动中的右移按键，如 D)
     */
    public boolean isInputRight() {
        return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.right).orElse(false);
    }

    /**
     * 获取玩家的按键输入是否为跳跃。
     * @return 玩家的按键输入是否为跳跃 (对应着移动中的跳跃按键，如 Space)
     */
    public boolean isInputJumping() {
        return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.jumping).orElse(false);
    }

    /**
     * 获取玩家当前是否正在匍匐
     * @return 玩家当前是否正在匍匐
     */
    public boolean isCrawl() {
        return processGunOperator(IClientPlayerGunOperator::isCrawl).orElse(false);
    }

    /**
     * 获取玩家是否接触地面
     * @return 玩家是否接触地面
     */
    public boolean isOnGround() {
        return processPlayer(Entity::onGround).orElse(false);
    }

    /**
     * 获取 玩家是否蹲伏
     * @return 玩家是否蹲伏
     */
    public boolean isCrouching() {
        return processPlayer(Entity::isCrouching).orElse(false);
    }

    /**
     * 在玩家当前的行走距离打上锚点。此后，getWalkDist() 将返回与此锚点的相对值
     */
    public void anchorWalkDist() {
        processPlayer(player -> {
            walkDistAnchor = player.walkDist + (player.walkDist - player.walkDistO) * partialTicks;
            return null;
        });
    }

    /**
     * 获取与锚点相对的行走距离。如果没有打锚点，则直接获取行走距离。
     * @return 与锚点相对的行走距离。如果没有打锚点，则直接返回行走距离。
     */
    public float getWalkDist() {
        return processPlayer(player -> {
            float currentWalkDist = player.walkDist + (player.walkDist - player.walkDistO) * partialTicks;
            return currentWalkDist - walkDistAnchor;
        }).orElse(0f);
    }

    /**
     * 状态机脚本请不要调用此方法。此方法用于状态机更新时设置当前的物品对象。
     */
    public void setCurrentGunItem(ItemStack currentGunItem) {
        this.currentGunItem = currentGunItem;
        this.iGun = IGun.getIGunOrNull(currentGunItem);
        if (iGun != null) {
            clientGunIndex = TimelessAPI.getClientGunIndex(iGun.getGunId(currentGunItem)).orElse(null);
        }
    }

    /**
     * 获取最后一次更新时的 partialTicks
     * @return 状态机最后一次更新的 partialTicks.
     */
    public float getPartialTicks() {
        return partialTicks;
    }

    /**
     * 状态机脚本请不要调用此方法。此方法用于状态机更新时设置 partialTicks。
     */
    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}
