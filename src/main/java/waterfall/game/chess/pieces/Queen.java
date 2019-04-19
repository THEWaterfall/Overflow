package waterfall.game.chess.pieces;

import waterfall.game.chess.ChessPiece;
import waterfall.game.chess.MoveRule;

public class Queen extends ChessPiece{

	public Queen(ChessPiece.PieceColor color){
		super(PieceType.Queen, color, validMoves(), true);
	}


	private static MoveRule[] validMoves(){
		return new MoveRule[]{	new MoveRule(1, 0, false, false), new MoveRule(0, 1, false, false),
                          new MoveRule(-1, 0, false, false), new MoveRule(0, -1, false, false),
                          new MoveRule(1, 1, false, false), new MoveRule(1, -1, false, false),
                          new MoveRule(-1, 1, false, false), new MoveRule(-1, -1, false, false)};
	}
}
