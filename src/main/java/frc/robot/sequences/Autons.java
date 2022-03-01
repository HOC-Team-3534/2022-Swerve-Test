package frc.robot.sequences;

import java.util.Arrays;
import java.util.List;

import frc.robot.Robot;
import frc.robot.sequences.parent.BaseAutonSequence;
import frc.robot.sequences.parent.BaseSequence;
import frc.robot.sequences.parent.IState;
import frc.robot.subsystems.parent.BaseSubsystem;

public class Autons extends BaseAutonSequence<AutonState> {

    public Autons(AutonState neutralState, AutonState startState) {
        super(neutralState, startState);
    }

    @Override
    public void process() {

        switch (getState()) {
            case MOVETOBALL1:
                break;
            case PICKUPBALL1:
                break;
            case MOVETOSHOOTBALL1:
                break;
            case SHOOTBALL1:
                break;
            case MOVETOBALL2:
                break;
            case PICKUPBALL2:
                break;
            case MOVETOBALL3:
                break;
            case PICKUPBALL3:
                break;
            case SHOOTBALL2:
                break;
            case NEUTRAL:
                break;
            default:
                break;

        }
        updateState();
    }

    @Override
    public boolean abort() {
        // TODO Auto-generated method stub
        return false;
    }

}

enum AutonState implements IState {
    NEUTRAL,
    MOVETOBALL1(Robot.swerveDrive),
    PICKUPBALL1(Robot.intake),
    MOVETOSHOOTBALL1(Robot.swerveDrive),
    SHOOTBALL1(Robot.shooter),
    MOVETOBALL2(Robot.swerveDrive),
    PICKUPBALL2(Robot.intake),
    MOVETOBALL3(Robot.swerveDrive),
    PICKUPBALL3(Robot.intake),
    MOVETOSHOOTBALL2(Robot.swerveDrive),
    SHOOTBALL2(Robot.shooter);

    List<BaseSubsystem> requiredSubsystems;

    AutonState(BaseSubsystem... subsystems) {
        requiredSubsystems = Arrays.asList(subsystems);
    }

    @Override
    public List<BaseSubsystem> getRequiredSubsystems() {
        return requiredSubsystems;
    }

    @Override
    public boolean requireSubsystems(BaseSequence<? extends IState> sequence) {
        for (BaseSubsystem subsystem : requiredSubsystems) {
            if (subsystem.isRequiredByAnother(sequence)) {
                return false;
            }
        }
        for (BaseSubsystem subsystem : requiredSubsystems) {
            subsystem.require(sequence, this);
        }
        return true;
    }

    @Override
    public String getName() {
        return this.name();
    }
}