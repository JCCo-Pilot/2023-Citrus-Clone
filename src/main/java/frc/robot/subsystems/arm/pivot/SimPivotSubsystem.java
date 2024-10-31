package frc.robot.subsystems.arm.pivot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.simulation.SingleJointedArmSim;
import frc.robot.constants.GameConstants;
import frc.robot.subsystems.arm.constants.ArmConstants;
import frc.robot.subsystems.arm.constants.ArmPIDs;

public class SimPivotSubsystem extends PivotSubsystem {
    private double position;
    private final ProfiledPIDController pivotPID;
    private final SingleJointedArmSim pivotSim;

    public SimPivotSubsystem() {
        position = ArmConstants.PivotConstants.DOWN_ANGLE;
        pivotPID = new ProfiledPIDController(
                ArmPIDs.pivotKp.get(),
                ArmPIDs.pivotKi.get(),
                ArmPIDs.pivotKd.get(),
                new TrapezoidProfile.Constraints(
                        ArmPIDs.pivotVelocity.get(),
                        ArmPIDs.pivotAcceleration.get()
                )
        );
        pivotSim = new SingleJointedArmSim(
                LinearSystemId.createSingleJointedArmSystem(
                        DCMotor.getNEO(2),
                        SingleJointedArmSim.estimateMOI(0.6, 30),
                        200
                ),
                DCMotor.getNEO(2),
                200,
                0.6,
                2 * Math.PI * -10 / 360,
                2 * Math.PI * 220 / 360,
                true,
                2 * Math.PI * -10 / 360
        );
    }

    @Override
    public void setPivot(ArmConstants.ArmSuperstructureState state, GameConstants.GamePiece gamePiece) {
        position = getTargetPosition(state, gamePiece);
    }

    @Override
    public void periodic() {
        updatePIDs();
        runPivot();
        pivotSim.update(0.020);
        pivotSim.setState(pivotSim.getAngleRads(), pivotSim.getVelocityRadPerSec());
    }

    @Override
    public void runPivot() {
        pivotPID.setGoal(Rotation2d.fromRotations(position).getRadians());
        pivotSim.setInputVoltage(pivotPID.calculate(pivotSim.getAngleRads()));
    }

    @Override
    protected boolean atState() {
        return MathUtil.isNear(Rotation2d.fromRotations(position).getRadians(), pivotSim.getAngleRads(), ArmConstants.PivotConstants.PIVOT_TOLERANCE);
    }

    public Rotation2d getCurrentAngle() {
        return Rotation2d.fromRadians(pivotSim.getAngleRads());
    }

    private void updatePIDs() {
        pivotPID.setPID(
                ArmPIDs.pivotKp.get(),
                ArmPIDs.pivotKi.get(),
                ArmPIDs.pivotKd.get()
        );
        pivotPID.setConstraints(
                new TrapezoidProfile.Constraints(
                        ArmPIDs.pivotVelocity.get(),
                        ArmPIDs.pivotAcceleration.get()
                )
        );
    }
}