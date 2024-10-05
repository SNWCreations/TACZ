package com.tacz.guns.client.animation.statemachine;

import com.tacz.guns.api.client.animation.statemachine.AnimationStateContext;

public class ItemAnimationStateContext extends AnimationStateContext {
    /**
     * 表示收起物品动画的建议时长，具体生效依赖于状态机的控制
     */
    private float putAwayTime = 0f;

    public float getPutAwayTime() {
        return putAwayTime;
    }

    public void setPutAwayTime(float putAwayTime) {
        this.putAwayTime = putAwayTime;
    }
}
