package net.fodoth.skina.goldentweaks.content.multiblock.basic_chemical_plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

/**
 * Maps the kettle's blockstate ({@code facing} + {@code roll}) onto the frame AMI's pattern
 * matcher expects.
 *
 * <p>The blockstate stores the front face plus a quarter turn around that face, which gives the
 * twenty-four physical orientations. AMI instead takes a {@code forwards} direction and an
 * {@code up} direction, which amounts to the same twenty-four, so the two are interchangeable.</p>
 *
 * <p><b>The relation between them.</b> AMI lays the pattern out as
 * {@code master + right*w - up*h + forwards*d} with {@code right = forwards x up} (negated when
 * mirrored). Our structure files are written bottom layer first, rows north to south and columns
 * west to east, so the first row is the one against the front face and later rows go deeper into
 * the machine. That makes the pattern's columns run along {@code right}, its layers along
 * {@code up} and its rows along {@code -forwards}, hence {@code forwards} is the facing
 * direction itself.</p>
 *
 * <p><b>Why {@link #baseUp} is not simply {@link Direction#UP}.</b> A machine whose front points
 * up or down has a horizontal "up" on that face: {@code R_x(+90)} maps the front from north to up
 * and takes the model's up vector to south, while the mirrored {@code R_x(-90)} sends it north.
 * Those two are the vanilla {@code x: 270} / {@code x: 90} model rotations the blockstate already
 * uses for {@code facing=up} and {@code facing=down}, so the values here follow the same
 * convention as the models and the two cannot drift apart.</p>
 */
public final class GTChemicalReactorOrientation {

    /** Number of quarter turns a machine can make around its own facing axis. */
    public static final int QUARTER_TURNS = 4;

    private GTChemicalReactorOrientation() {
    }

    /** Up direction of the front face before rolling, for the given facing. */
    public static Direction baseUp(Direction facing) {
        return switch (facing) {
            case UP -> Direction.SOUTH;
            case DOWN -> Direction.NORTH;
            default -> Direction.UP;
        };
    }

    /**
     * Up direction of the front face after {@code roll} quarter turns clockwise around
     * {@code facing}.
     */
    public static Direction up(Direction facing, int roll) {
        Direction up = baseUp(facing);
        int turns = Math.floorMod(-roll, QUARTER_TURNS);
        for (int turn = 0; turn < turns; turn++) {
            up = cross(facing, up);
        }
        return up;
    }

    /** The quarter turn that produces {@code up} on the given facing, or zero if it is invalid. */
    public static int roll(Direction facing, Direction up) {
        for (int roll = 0; roll < QUARTER_TURNS; roll++) {
            if (up(facing, roll) == up) {
                return roll;
            }
        }
        return 0;
    }

    /** Whether {@code up} is a legal front-face up direction for {@code facing}. */
    @SuppressWarnings("unused")
    public static boolean isValidUp(Direction facing, Direction up) {
        return up.getAxis() != facing.getAxis();
    }

    /** Unit-vector cross product of two axis directions; equals a positive quarter turn. */
    public static Direction cross(Direction a, Direction b) {
        Vec3i product = new Vec3i(a.getStepX(), a.getStepY(), a.getStepZ())
                .cross(new Vec3i(b.getStepX(), b.getStepY(), b.getStepZ()));
        Direction result = Direction.fromDelta(product.getX(), product.getY(), product.getZ());
        return result != null ? result : a;
    }

    /** Right-hand side of the front face: {@code forwards x up}. */
    public static Direction right(Direction facing, Direction up) {
        return cross(facing, up);
    }

    /**
     * The pattern's {@code forwards} axis for a host facing the given way.
     *
     * <p>These are opposite, which is not obvious from AMI's names: its {@code forwards} points
     * from the structure <em>toward</em> the viewer, while our blockstate's {@code facing} points
     * the way the machine looks. Treating them as the same put the whole shape behind the host —
     * a host placed facing the player could only form when it was turned away. Flipping here keeps
     * the builder, the matcher and the projection on one convention.</p>
     */
    public static Direction patternForwards(Direction facing) {
        return facing.getOpposite();
    }

    /**
     * World position of a layout cell.
     *
     * <p>AMI addresses cells as {@code anchor + right*w - up*h + forwards*d}, with {@code h}
     * counted downward from the top and {@code d} counted away from the front face. Our layout
     * stores layers bottom-up and rows north-to-south, and its first row is the one furthest from
     * the front, so both indices flip.</p>
     *
     * <p>Shared on purpose: the builder, the structure matcher and the projection all place cells,
     * and a drift between them would silently put the projection or the built casing somewhere
     * else than the matcher looks.</p>
     */
    public static BlockPos cell(BlockPos anchor, Direction forwards, Direction up, boolean mirrored,
                                int height, int depth, int column, int layer, int row) {
        Direction right = right(forwards, up);
        if (mirrored) {
            right = right.getOpposite();
        }
        int downFromTop = height - 1 - layer;
        int awayFromFront = depth - 1 - row;
        return anchor
                .relative(right, column)
                .relative(up, -downFromTop)
                .relative(forwards, awayFromFront);
    }

    /**
     * Translates the master position to the pattern's anchor point.
     *
     * <p>AMI's matcher is written in terms of {@code frontTopLeft}, not the master cell:
     * {@code matches} walks every pattern cell as
     * {@code frontTopLeft + right*w - up*h + forwards*d}, and its internal search converts the
     * master position to that anchor first. Passing the master position directly shifts the whole
     * structure by the master's offset, so no cell lines up and nothing ever matches.</p>
     *
     * <p>This mirrors {@code AMiBlockPattern.translateAndRotate} exactly, including the mirrored
     * sign flip on the right vector, because that method is {@code protected} and cannot be
     * called from here.</p>
     *
     * @param masterPos the host's own position
     * @param masterX   master column within the pattern
     * @param masterY   master height within the pattern
     * @param masterZ   master depth within the pattern
     */
    public static BlockPos frontTopLeft(BlockPos masterPos, Direction forwards, Direction up,
                                        boolean mirrored, int masterX, int masterY, int masterZ) {
        Direction right = right(forwards, up);
        if (mirrored) {
            right = right.getOpposite();
        }
        // Anchor = master - right*masterX + up*masterY - forwards*masterZ, which is
        // translateAndRotate(masterPos, forwards, up, mirrored, -masterX, -masterY, -masterZ).
        return masterPos
                .relative(right, -masterX)
                .relative(up, masterY)
                .relative(forwards, -masterZ);
    }
}
