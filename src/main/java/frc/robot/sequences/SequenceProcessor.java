package frc.robot.sequences;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Robot;
import frc.robot.RobotContainer.Buttons;
import frc.robot.RobotMap;

public class SequenceProcessor{

    /**
     * Create a new variable of each of the functions
     */

    public Drive drive;
    public Shoot shoot;
    public Burp burp;
    public IntakeSeq intake;
    public Extake extake;
    public ClimbPrep climbPrep;
    public Climb climb;
    public ClimbPrepReset climbPrepReset;
    public ClimbReset climbReset;

    public SequenceProcessor() {

        /**
         * Instantiate each of the sequences
         */

        drive = new Drive(DriveState.NEUTRAL, DriveState.DRIVE);
        
        shoot = new Shoot(ShootState.NEUTRAL, ShootState.WAITNSPIN);
        burp = new Burp(BurpState.NEUTRAL, BurpState.BURP);
        intake = new IntakeSeq(IntakeState.NEUTRAL, IntakeState.EXTEND);
        extake = new Extake(ExtakeState.NEUTRAL, ExtakeState.EXTAKE);
        climbPrep = new ClimbPrep(ClimbPrepState.NEUTRAL, ClimbPrepState.PREPCLAW);
        climb = new Climb(ClimbState.NEUTRAL, ClimbState.GRIPMIDBAR);
        climbPrepReset = new ClimbPrepReset(ClimbPrepResetState.NEUTRAL, ClimbPrepResetState.RESETARM);
        climbReset = new ClimbReset(ClimbResetState.NEUTRAL, ClimbResetState.MOVEARMMANUALLY);
    }

    public void process() {

        Robot.swerveDrive.resetTXOffset();
        
        if (climb.isNeutral() && shoot.isNeutral()) {
            drive.start(Robot.swerveDrive);
        }
        if(Buttons.RAMPSHOOTER.getButton() || Buttons.SHOOT.getButton()) {
            shoot.start(Robot.swerveDrive);
            RobotMap.limelight.resetLimelight();
        }
        if(Buttons.Burp.getButton()){
            burp.start();
        }
        if(Buttons.Intake.getButton()) {
            intake.start();
        }
        if(Buttons.Extake.getButton()){
            extake.start();
        }
        if (Buttons.ClimbPrep.getButton()) { // TODO: in last 35 seconds of match logic
            climbPrep.start();
        }
        if (Buttons.Climb.getButton()
                && Robot.climber.getSequenceRequiring().getState().getName() == "PREPPEDFORCLIMB"
                && (!RobotMap.m_l1Switch.get() || !RobotMap.m_h2Switch.get())) {
            climb.start(Robot.climber, Robot.swerveDrive);
        }
        if(Buttons.ClimbPrepReset.getButton() && !climbPrep.isNeutral()){
            climbPrepReset.start(Robot.climber);
        }
        if(Buttons.MoveClimbArmManually.getButton()){
            climbReset.start();
        }

        drive.process();
        shoot.process();
        burp.process();
        intake.process();
        extake.process();
        climbPrep.process();
        climbPrepReset.process();
        climb.process();
        climbReset.process();
        SmartDashboard.putBoolean("Prepped for climb: ", climbPrep.getState().getName() == "PREPPEDFORCLIMB");
        SmartDashboard.putString("Climb State", climb.getState().getName());
    }
}
