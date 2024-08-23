package com.tacz.guns.api.client.animation.statemachine;

import com.tacz.guns.api.client.animation.AnimationController;
import com.tacz.guns.api.client.animation.DiscreteTrackArray;
import com.tacz.guns.api.client.animation.ObjectAnimationRunner;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AnimationStateContext {
    private @Nullable AnimationStateMachine<?> stateMachine;
    private final DiscreteTrackArray trackArray = new DiscreteTrackArray();

    public @Nullable AnimationStateMachine<?> getStateMachine() {
        return stateMachine;
    }

    public DiscreteTrackArray getTrackArray() {
        return trackArray;
    }

    /**
     * 分配一个新的轨道行，返回新的轨道行下标。
     * @return 新的轨道行下标
     * @throws TrackArrayMismatchException 当状态机对应的 track array 不是当前 context 指定的实例，抛出此异常。
     */
    public int addTrackLine() {
        checkTrackArray();
        return getTrackArray().addTrackLine();
    }

    /**
     * 确保轨道行的数量
     * @param size 需要确保的轨道行数量。
     * @throws TrackArrayMismatchException 当状态机对应的 track array 不是当前 context 指定的实例，抛出此异常。
     */
    public void ensureTrackLineSize(int size) {
        checkTrackArray();
        getTrackArray().ensureCapacity(size);
    }

    /**
     * @return 轨道行的数量
     * @throws TrackArrayMismatchException 当状态机对应的 track array 不是当前 context 指定的实例，抛出此异常。
     */
    public int getTrackLineSize() {
        checkTrackArray();
        return getTrackArray().getTrackLineSize();
    }

    /**
     * 为指定轨道行分配一个新的轨道，返回新的轨道的下标
     * @param index 轨道行的下标
     * @return 新的轨道下标
     * @throws TrackArrayMismatchException 当状态机对应的 track array 不是当前 context 指定的实例，抛出此异常。
     */
    public int assignNewTrack(int index) {
        checkTrackArray();
        return getTrackArray().assignNewTrack(index);
    }

    /**
     * 优先返回轨道行中的空闲轨道，如果没有空闲轨道则会开辟一个新的轨道
     * @param index 轨道行的下标
     * @param interruptHolding 是否将处于 holding 状态的轨道视为空闲轨道
     * @return 轨道在控制器中的指针
     * @throws TrackArrayMismatchException 当状态机对应的 track array 不是当前 context 指定的实例，抛出此异常。
     * @see AnimationStateContext#assignNewTrack(int)
     */
    public int findIdleTrack(int index, boolean interruptHolding) {
        if (stateMachine == null) {
            throw new IllegalStateException("This context has not been bound to a state machine.");
        }
        checkTrackArray();
        DiscreteTrackArray trackArray = getTrackArray();
        List<Integer> trackList = trackArray.getByIndex(index);
        AnimationController controller = stateMachine.getAnimationController();
        for (int track : trackList) {
            ObjectAnimationRunner animation = controller.getAnimation(track);
            if (animation == null || animation.isStopped() || (interruptHolding && animation.isHolding())) {
                return track;
            }
        }
        return trackArray.assignNewTrack(index);
    }

    /**
     * 保证指定的轨道行有足够的轨道数量
     * @param index 轨道行下标
     * @param amount 需要的轨道数量
     */
    public void ensureTracksAmount(int index, int amount) {
        checkTrackArray();
        getTrackArray().ensureTrackAmount(index, amount);
    }

    /**
     *
     * @param trackLineIndex 轨道行的下标
     * @param trackIndex 轨道的下标
     * @return 轨道在控制器中的指针，或者 -1 当轨道在
     */
    public int getTrack(int trackLineIndex, int trackIndex) {
        checkTrackArray();
        DiscreteTrackArray trackArray = getTrackArray();
        if (trackLineIndex >= trackArray.getTrackLineSize()) {
            return -1;
        }
        List<Integer> tracks = trackArray.getByIndex(trackLineIndex);
        if (trackIndex >= tracks.size()) {
            return -1;
        }
        return tracks.get(trackIndex);
    }

    /**
     * 用于只需要一个轨道的轨道行，如果目标轨道行没有轨道，则会分配一个轨道，
     * 如果已经有多个轨道，多余的轨道不会舍弃，会返回其中的第一个轨道。
     * @param index 轨道行的下标
     * @throws TrackArrayMismatchException 当状态机对应的 track array 不是当前 context 指定的实例，抛出此异常。
     * @return 轨道的下标
     */
    public int getAsSingletonTrack(int index) {
        checkTrackArray();
        DiscreteTrackArray trackArray = getTrackArray();
        List<Integer> trackList = trackArray.getByIndex(index);
        if (trackList.isEmpty()) {
            return trackArray.assignNewTrack(index);
        } else {
            return trackList.get(0);
        }
    }

    void setStateMachine(@Nullable AnimationStateMachine<?> stateMachine) {
        if (this.stateMachine != null) {
            this.stateMachine.getAnimationController().setUpdatingTrackArray(null);
        }
        if (stateMachine != null) {
            stateMachine.getAnimationController().setUpdatingTrackArray(trackArray);
        }
        this.stateMachine = stateMachine;
    }

    private void checkTrackArray() {
        if (stateMachine != null && stateMachine.getAnimationController().getUpdatingTrackArray() != trackArray) {
            throw new TrackArrayMismatchException();
        }
    }
}
