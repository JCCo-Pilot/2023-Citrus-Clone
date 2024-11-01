package frc.robot.subsystems.arm.pivot;

import com.revrobotics.CANSparkBase;
import com.revrobotics.CANSparkLowLevel;
import com.revrobotics.CANSparkMax;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.DigitalInput;
import frc.lib.NewDutyCycleEncoder;
import frc.robot.subsystems.arm.constants.ArmConstants;
import frc.robot.subsystems.arm.constants.ArmConstants.ArmSuperstructureState;
import frc.robot.subsystems.arm.constants.ArmPIDs;
import frc.robot.util.Helpers;
import frc.robot.constants.GameConstants.GamePiece;

public class ConcretePivotSubsystem extends PivotSubsystem {
    private final CANSparkMax leftPivot, rightPivot;
    private final NewDutyCycleEncoder encoder;
    private final ProfiledPIDController pidController;

    public ConcretePivotSubsystem() {
        leftPivot = new CANSparkMax(
                ArmConstants.IDs.LEFT_PIVOT_ID,
                CANSparkLowLevel.MotorType.kBrushless
        );
        leftPivot.setIdleMode(CANSparkBase.IdleMode.kCoast);
        rightPivot = new CANSparkMax(
                ArmConstants.IDs.RIGHT_PIVOT_ID,
                CANSparkLowLevel.MotorType.kBrushless
        );
        rightPivot.follow(leftPivot, true);
        rightPivot.setIdleMode(CANSparkBase.IdleMode.kCoast);

        encoder = new NewDutyCycleEncoder(
                new DigitalInput(ArmConstants.IDs.PIVOT_ENCODER_ID),
                4,
                -ArmConstants.PivotConstants.DOWN_ANGLE
        );
        pidController = new ProfiledPIDController(
                ArmPIDs.pivotKp.get(),
                ArmPIDs.pivotKi.get(),
                ArmPIDs.pivotKd.get(),
                new TrapezoidProfile.Constraints(
                        ArmPIDs.pivotVelocity.get(),
                        ArmPIDs.pivotAcceleration.get()
                )
        );
    }

    public void setPivot(ArmSuperstructureState state, GamePiece gamePiece) {
        pidController.setGoal(
                switch (state) {
                    case IDLE -> ArmConstants.PivotConstants.DOWN_ANGLE;
                    case GROUND_INTAKING -> ArmConstants.PivotConstants.GROUND_INTAKING_ANGLE;
                    case SUBSTATION_INTAKING -> ArmConstants.PivotConstants.SUBSTATION_INTAKING_ANGLE;
                    default -> gamePiece == GamePiece.CONE ?
                            ArmConstants.PivotConstants.CONE_PIVOT_ANGLE :
                            ArmConstants.PivotConstants.CUBE_PIVOT_ANGLE;
                });
    }

    public void runPivot() {
//        only uncomment after setting upper and lower values
//        leftPivot.set(pidController.calculate(
//                encoder.get()
//        ));
    }

    protected boolean atState() {
        return Helpers.withinTolerance(
                pidController.getGoal().position,
                encoder.get(),
                ArmConstants.PivotConstants.PIVOT_TOLERANCE);
    }

    public Rotation2d getAngle() {
        return Rotation2d.fromRotations(encoder.get());
    }

    @Override
    public void periodic() {
        pidController.setPID(
                ArmPIDs.pivotKp.get(),
                ArmPIDs.pivotKi.get(),
                ArmPIDs.pivotKd.get()
        );
        pidController.setConstraints(
                new TrapezoidProfile.Constraints(
                        ArmPIDs.pivotVelocity.get(),
                        ArmPIDs.pivotAcceleration.get()
                )
        );
    }
}
