package waterfall.game.chess.pieces;

import waterfall.game.Color;
import waterfall.game.chess.ChessPiece;
import waterfall.game.chess.MoveRule;

public class Queen extends ChessPiece{
	private final static float cost = 9;

	public Queen(Color color){
		super(PieceType.Queen, color, validMoves(), true);
	}


	private static MoveRule[] validMoves(){
		return new MoveRule[]{	new MoveRule(1, 0, false, false), new MoveRule(0, 1, false, false),
                          new MoveRule(-1, 0, false, false), new MoveRule(0, -1, false, false),
                          new MoveRule(1, 1, false, false), new MoveRule(1, -1, false, false),
                          new MoveRule(-1, 1, false, false), new MoveRule(-1, -1, false, false)};
	}

	@Override
	public float getCost() {
		return cost;
	}
}
