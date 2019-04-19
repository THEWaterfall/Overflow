package waterfall.game.chess.pieces;

import waterfall.game.chess.ChessPiece;
import waterfall.game.chess.MoveRule;

public class Pawn extends ChessPiece {

	public Pawn(PieceColor color){
		super(PieceType.Pawn, color, validMoves(color), false);
	}

	private static MoveRule[] validMoves(PieceColor color){
        if (color == PieceColor.Black){
            return new MoveRule[]{new MoveRule(0, 1, false, false), new MoveRule(0, 2, true, false),
                              new MoveRule(1, 1, false, true), new MoveRule(-1, 1, false, true)};
        } else {
            return new MoveRule[]{new MoveRule(0, -1, false, false), new MoveRule(0, -2, true, false),
                              new MoveRule(1, -1, false, true), new MoveRule(-1, -1, false, true)};
        }
	}
}
