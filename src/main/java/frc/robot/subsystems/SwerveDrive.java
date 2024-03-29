package frc.robot.subsystems;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.wpilibj.drive.Vector2d;
import frc.robot.Constants;
import frc.robot.Robot;
import frc.robot.RobotContainer.Buttons;
import frc.robot.RobotContainer.Axes;
import frc.robot.RobotMap;
import frc.robot.autons.pathplannerfollower.CalculatedDriveVelocities;
import frc.robot.autons.pathplannerfollower.PathStateController;
import frc.robot.subsystems.parent.BaseDriveSubsystem;
import frc.robot.subsystems.states.SwerveDriveState;

public class SwerveDrive extends BaseDriveSubsystem<SwerveDriveState> {

	private double frontLeft_stateAngle = 0.0,
			frontRight_stateAngle = 0.0,
			backLeft_stateAngle = 0.0,
			backRight_stateAngle = 0.0;

	PIDController xPID = new PIDController(20,0,0);
	PIDController yPID = new PIDController(20,0,0);
	PIDController rotPID = new PIDController(8,0,0);

	PathStateController pathStateController = new PathStateController(xPID, yPID, rotPID);

	public SwerveDrive() {
		super(RobotMap.m_frontLeftModule, RobotMap.m_frontRightModule, RobotMap.m_backLeftModule,
				RobotMap.m_backRightModule, SwerveDriveState.NEUTRAL);
		setPathStateController(pathStateController);
	}

	PIDController limelightPID = new PIDController(0.185, 0.0, 0.0);
	Rotation2d targetShootRotationAngle = new Rotation2d();

	@Override
	public void process() {

		super.process();

		switch(getCurrentSubsystemState()){
			case NEUTRAL:
				neutral();
				break;
			case DRIVE:
				if ((Buttons.Creep.getButton())) {
					creep();
				} else {
					drive();
				}
				break;
			case AIM:
				if (RobotMap.limelight.isTargetAcquired() && Buttons.SHOOT.getButton()) {
					aim();
				}else if(RobotMap.limelight.isTargetAcquired() && Robot.isAutonomous){
					aim();
				}else{
					if(Buttons.Creep.getButton()){
						creep();
					}else{
						drive();
					}
				}
				break;
			case DRIVE_AUTONOMOUSLY:
				if (getStateFirstRunThrough()) {
					// TODO check if the start of the path is near current odometry for safety
				}
				if (this.getPathStateController().getPathPlannerFollower() != null) {
					driveOnPath();
				} else {
					System.out.println(
							"DRIVE PATH NOT SET. MUST CREATE PATHPLANNERFOLLOWER IN AUTON AND SET IN SWERVEDRIVE SUBSYSTEM");
				}
				break;
		}
	}

	public Vector2d getRobotCentricVelocity(){
		ChassisSpeeds chassisSpeeds = this.getSwerveDriveKinematics().toChassisSpeeds(this.getSwerveModuleStates());
		return new Vector2d(chassisSpeeds.vxMetersPerSecond, chassisSpeeds.vyMetersPerSecond);
	}

	public Vector2d getTargetOrientedVelocity(){
		Vector2d robotCentricVelocity = getRobotCentricVelocity();
		robotCentricVelocity.rotate(180.0);
		return robotCentricVelocity;
	}

	public void setTargetShootRotationAngle() {;
		//this.targetShootRotationAngle = getGyroHeading().plus(RobotMap.limelight.getLimelightShootProjection().getOffset());
		this.targetShootRotationAngle = getGyroHeading().plus(RobotMap.limelight.getHorizontalAngleOffset());
	}

	public Rotation2d getTargetShootRotationAngleError(){ return targetShootRotationAngle.minus(getGyroHeading()); }

	@Override
	public Rotation2d getGyroHeading() {
		return RobotMap.pigeon.getRotation2d().plus(this.getGyroOffset());
	}

	@Override
	public void resetGyro() {
		RobotMap.pigeon.reset();
	}

	private void drive(){
		drive(Axes.Drive_ForwardBackward.getAxis() * Constants.MAX_VELOCITY_METERS_PER_SECOND,
				Axes.Drive_LeftRight.getAxis() * Constants.MAX_VELOCITY_METERS_PER_SECOND,
				Axes.Drive_Rotation.getAxis() * Constants.MAX_ANGULAR_VELOCITY_RADIANS_PER_SECOND,
				true);
	}

	private void creep(){
		drive(Axes.Drive_ForwardBackward.getAxis() * Constants.MAX_VELOCITY_CREEP_METERS_PER_SECOND,
				Axes.Drive_LeftRight.getAxis() * Constants.MAX_VELOCITY_CREEP_METERS_PER_SECOND,
				Axes.Drive_Rotation.getAxis() * Constants.MAX_ANGULAR_VELOCITY_CREEP_RADIANS_PER_SECOND,
				true);
	}

	private void aim(){
		double angleError = getTargetShootRotationAngleError().getDegrees();
		if(angleError > 2.0){
			angleError = 2.0;
		}else if(angleError < -2.0){
			angleError = -2.0;
		}
		double pidOutput = limelightPID.calculate(-angleError,0.0);
		drive(Axes.Drive_ForwardBackward.getAxis() * Constants.MAX_VELOCITY_CREEP_METERS_PER_SECOND,
				Axes.Drive_LeftRight.getAxis() * Constants.MAX_VELOCITY_CREEP_METERS_PER_SECOND,
				pidOutput * Constants.MAX_ANGULAR_VELOCITY_CREEP_RADIANS_PER_SECOND,
				true);
	}

	public void drive(double xSpeed, double ySpeed, double rot, boolean fieldRelative) {
		var swerveModuleStates = getSwerveDriveKinematics().toSwerveModuleStates(
				fieldRelative ? ChassisSpeeds.fromFieldRelativeSpeeds(
						xSpeed, ySpeed, rot, getGyroHeading())
						: new ChassisSpeeds(xSpeed, ySpeed, rot));
		SwerveDriveKinematics.desaturateWheelSpeeds(swerveModuleStates, Constants.MAX_VELOCITY_METERS_PER_SECOND);
		if (Math.abs(swerveModuleStates[0].speedMetersPerSecond) + Math.abs(swerveModuleStates[1].speedMetersPerSecond)
				+ Math.abs(swerveModuleStates[2].speedMetersPerSecond)
				+ Math.abs(swerveModuleStates[3].speedMetersPerSecond) > 0.001) {
			frontLeft_stateAngle = swerveModuleStates[0].angle.getRadians();
			frontRight_stateAngle = swerveModuleStates[1].angle.getRadians();
			backLeft_stateAngle = swerveModuleStates[2].angle.getRadians();
			backRight_stateAngle = swerveModuleStates[3].angle.getRadians();
		}
		RobotMap.m_frontLeftModule.set(swerveModuleStates[0].speedMetersPerSecond
				/ Constants.MAX_VELOCITY_METERS_PER_SECOND * Constants.MAX_VOLTAGE,
				frontLeft_stateAngle);
		RobotMap.m_frontRightModule.set(swerveModuleStates[1].speedMetersPerSecond
				/ Constants.MAX_VELOCITY_METERS_PER_SECOND * Constants.MAX_VOLTAGE,
				frontRight_stateAngle);
		RobotMap.m_backLeftModule.set(swerveModuleStates[2].speedMetersPerSecond
				/ Constants.MAX_VELOCITY_METERS_PER_SECOND * Constants.MAX_VOLTAGE,
				backLeft_stateAngle);
		RobotMap.m_backRightModule.set(swerveModuleStates[3].speedMetersPerSecond
				/ Constants.MAX_VELOCITY_METERS_PER_SECOND * Constants.MAX_VOLTAGE,
				backRight_stateAngle);
	}

	public void driveAutonomously(double xSpeed, double ySpeed, double rot, boolean fieldRelative) {
		var swerveModuleStates = getSwerveDriveKinematics().toSwerveModuleStates(
				fieldRelative ? ChassisSpeeds.fromFieldRelativeSpeeds(
						xSpeed, ySpeed, rot, getGyroHeading())
						: new ChassisSpeeds(xSpeed, ySpeed, rot));
		SwerveDriveKinematics.desaturateWheelSpeeds(swerveModuleStates,
				Constants.MAX_VELOCITY_METERS_PER_SECOND_AUTONOMOUS);
		if (Math.abs(swerveModuleStates[0].speedMetersPerSecond) + Math.abs(swerveModuleStates[1].speedMetersPerSecond)
				+ Math.abs(swerveModuleStates[2].speedMetersPerSecond)
				+ Math.abs(swerveModuleStates[3].speedMetersPerSecond) > 0.001) {
			frontLeft_stateAngle = swerveModuleStates[0].angle.getRadians();
			frontRight_stateAngle = swerveModuleStates[1].angle.getRadians();
			backLeft_stateAngle = swerveModuleStates[2].angle.getRadians();
			backRight_stateAngle = swerveModuleStates[3].angle.getRadians();
		}
		RobotMap.m_frontLeftModule.set(swerveModuleStates[0].speedMetersPerSecond
				/ Constants.MAX_VELOCITY_METERS_PER_SECOND_AUTONOMOUS * Constants.MAX_VOLTAGE
				* Constants.AUTON_MAX_VELOCITY_RATIO,
				frontLeft_stateAngle);
		RobotMap.m_frontRightModule.set(swerveModuleStates[1].speedMetersPerSecond
				/ Constants.MAX_VELOCITY_METERS_PER_SECOND_AUTONOMOUS * Constants.MAX_VOLTAGE
				* Constants.AUTON_MAX_VELOCITY_RATIO,
				frontRight_stateAngle);
		RobotMap.m_backLeftModule.set(swerveModuleStates[2].speedMetersPerSecond
				/ Constants.MAX_VELOCITY_METERS_PER_SECOND_AUTONOMOUS * Constants.MAX_VOLTAGE
				* Constants.AUTON_MAX_VELOCITY_RATIO,
				backLeft_stateAngle);
		RobotMap.m_backRightModule.set(swerveModuleStates[3].speedMetersPerSecond
				/ Constants.MAX_VELOCITY_METERS_PER_SECOND_AUTONOMOUS * Constants.MAX_VOLTAGE
				* Constants.AUTON_MAX_VELOCITY_RATIO,
				backRight_stateAngle);
	}

	private void driveOnPath() {
		CalculatedDriveVelocities velocities = this.getPathStateController()
				.getVelocitiesAtCurrentState(this.getSwerveDriveOdometry(), this.getGyroHeading());

		Translation2d currentPosition = getSwerveDriveOdometry().getPoseMeters().getTranslation();
		// System.out.println(String.format("Current Odometry [ X: %.2f Y:%.2f ] Heading
		// [ Rot (radians): %.2f ]", currentPosition.getX(), currentPosition.getY(),
		// getGyroHeading().getRadians()));
		// System.out.println("Current Velocity Calculations: " +
		// velocities.toString());
		driveAutonomously(velocities.getXVel(), velocities.getYVel(), velocities.getRotVel(), true);
	}

	@Override
	public void neutral() {
		drive(0.0, 0.0, 0.0, false);
	}

	@Override
	public boolean abort() {
		drive(0.0, 0.0, 0.0, false);
		return true;
	}
}

