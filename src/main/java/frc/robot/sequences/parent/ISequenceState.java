package frc.robot.sequences.parent;

import java.util.List;
import java.util.Set;

import frc.robot.subsystems.parent.BaseSubsystem;
import frc.robot.subsystems.parent.ISubsystemState;
import frc.robot.subsystems.parent.SubsystemRequirement;

import javax.management.InstanceNotFoundException;

public interface ISequenceState {

    Set<BaseSubsystem> getRequiredSubsystems();

    boolean requireSubsystems(BaseSequence<? extends ISequenceState> sequence);

    static boolean requireSubsystems(BaseSequence<? extends ISequenceState> sequence, List<SubsystemRequirement> requirements) {
        for (SubsystemRequirement subsystemRequirement : requirements) {
            if (subsystemRequirement.getSubsystem().isRequiredByAnother(sequence)) {
                return false;
            }
        }
        for (SubsystemRequirement subsystemRequirement : requirements) {
            subsystemRequirement.getSubsystem().require(sequence, getSubsystemState(subsystemRequirement.getSubsystem(), requirements));
        }
        return true;
    }

    static <BaseS extends BaseSubsystem, SsS extends ISubsystemState<BaseS>> SsS getSubsystemState(BaseS subsystem, List<SubsystemRequirement> requirements){
        for (SubsystemRequirement requirement : requirements) {
            if(requirement.getSubsystem().equals(subsystem)){
                return (SsS) requirement.getSubsystemState();
            }
        }
        return null;
    }
}