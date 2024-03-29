package frc.robot.subsystems.parent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import frc.robot.sequences.parent.BaseSequence;
import frc.robot.sequences.parent.ISequenceState;

public abstract class BaseSubsystem<SsS extends ISubsystemState> implements ISubsystem {

    boolean required;
    BaseSequence<? extends ISequenceState> sequenceRequiring;
    boolean stateChanged;
    boolean stateFirstRunThrough;

    SsS neutralState;
    SsS currentSubsystemState;

    Map<DoubleSolenoid, List<Long>> solenoidSetTimes = new HashMap<>();

    public BaseSubsystem(SsS neutralState){
        this.neutralState = neutralState;
        this.currentSubsystemState = neutralState;
    }

    public boolean isRequiredByAnother(BaseSequence<? extends ISequenceState> sequence) {
        if (sequenceRequiring == sequence) {
            return false;
        }
        return this.required;
    }

    public boolean require(BaseSequence<? extends ISequenceState> sequence, SsS subsystemState) {
        if (!isRequiredByAnother(sequence)) {
            required = true;
            setSequenceRequiring(sequence);
            setCurrentSubsystemState(subsystemState);
            return true;
        } else if (sequenceRequiring == sequence) {
            setCurrentSubsystemState(subsystemState);
            return true;
        } else {
            return false;
        }
    }

    public void process() {
        isStillRequired();
        checkStateChanged();
        checkToTurnOff();
    }

    private void setSequenceRequiring(BaseSequence<? extends ISequenceState> sequence) {
        this.sequenceRequiring = sequence;
    }

    private boolean isStillRequired() {
        if (!required) {
            return false;
        } else if (!sequenceRequiring.getState().getRequiredSubsystems().contains(this)) {
            release();
            return false;
        } else {
            return true;
        }
    }

    private void checkStateChanged() {
        stateFirstRunThrough = stateChanged;
        stateChanged = false;
    }

    public boolean getStateFirstRunThrough() {
        return this.stateFirstRunThrough;
    }

    void release() {
        required = false;
        sequenceRequiring = null;
        setCurrentSubsystemState(neutralState);
    }

    public boolean forceRelease() {
        if (this.getSequenceRequiring() == null) {
            return true;
        }
        if (this.abort()) {
            if (this.getSequenceRequiring().reset()) {
                release();
                return true;
            }
        }
        return false;
    }

    public BaseSequence<? extends ISequenceState> getSequenceRequiring() {
        return sequenceRequiring;
    }

    public void setWithADelayToOff(DoubleSolenoid ds, DoubleSolenoid.Value value, long millisUntilOff) {
        //solenoidSetTimes.put(ds, Arrays.asList(System.currentTimeMillis(), millisUntilOff));
        ds.set(value);
    }

    private boolean checkToTurnOff() {
        List<DoubleSolenoid> removeList = new ArrayList<DoubleSolenoid>();
        boolean setToOff = false;
        for (DoubleSolenoid ds : solenoidSetTimes.keySet()) {
            System.out.println("I AM SOLENOID");
            List<Long> times = solenoidSetTimes.get(ds);
            if (ds.get() != DoubleSolenoid.Value.kOff
                    && System.currentTimeMillis() - times.get(0) >= times.get(1)) {
                //ds.set(DoubleSolenoid.Value.kOff);
                System.out.println("SET SOLENOID TO OFF");
                removeList.add(ds);
                setToOff = true;
            }
        }
        for (DoubleSolenoid ds : removeList) {
            solenoidSetTimes.remove(ds);
        }
        return setToOff;
    }

    private void setCurrentSubsystemState(SsS state){
        stateChanged = true;
        this.currentSubsystemState = state;
    }

    public SsS getCurrentSubsystemState(){
        return this.currentSubsystemState;
    }
}
