package jsettlers.graphics.map;

import go.graphics.GLDrawContext;

import java.awt.Color;

import jsettlers.common.map.IHexMap;
import jsettlers.common.map.IHexTile;
import jsettlers.common.map.shapes.IMapArea;
import jsettlers.common.movable.EDirection;
import jsettlers.common.position.ISPosition2D;
import jsettlers.common.position.IntRectangle;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.graphics.map.geometry.MapCoordinateConverter;

/**
 * This is the drawing context for a map. It is used to translate the visible
 * screen space to local coordinate space and holds the current gl context.
 * <p>
 * This class uses 3 coordiante systems and provides conversion methods between
 * them.
 * <ul>
 * <li>The map space. It is the map coordinate system (x, y)
 * <li>The draw space (used by all draw stuff).
 * <li>The screen space (visible space on which is drawn).
 * <ul>
 * <h2>gl setup</h2> With {@link #begin(GLDrawContext)}, the gl state machine is
 * initialized for drawing the map. The draw coordinates can then be given in
 * draw space.
 * 
 * @author michael
 */
public class MapDrawContext {

	// private static final byte HEIGHT_FACTOR = 4;

	private GLDrawContext gl = null;

	private final IHexMap map;

	private ScreenPosition screen;

	private MapCoordinateConverter converter;
	Color[] playerColors = new Color[] {
	        Color.RED,
	        Color.BLUE,
	        Color.ORANGE,
	        Color.YELLOW,
	        Color.BLACK,
	        Color.WHITE,
	        Color.GRAY,
	        Color.PINK,
	        Color.CYAN,
	        Color.LIGHT_GRAY,
	        Color.DARK_GRAY,
	};

	// private long beginTime;

	/**
	 * Creates a new map context for a given map.
	 * 
	 * @param map
	 *            The map.
	 */
	public MapDrawContext(IHexMap map) {
		this.map = map;
		float incline = IHexTile.X_DISTANCE / 2.0f / IHexTile.Y_DISTANCE;
		int mapHeight = map.getHeight() * IHexTile.Y_DISTANCE;
		int mapWidth = map.getWidth() * IHexTile.X_DISTANCE;
		this.screen = new ScreenPosition(mapWidth, mapHeight, incline);

		this.converter =
		        MapCoordinateConverter.get(IHexTile.X_DISTANCE,
		                IHexTile.Y_DISTANCE, map.getWidth(), map.getHeight());

	}

	/**
	 * Sets the size of the context to width/height.
	 * 
	 * @param width
	 *            The width.
	 * @param height
	 *            The height.
	 */
	public void setSize(int width, int height) {
		this.screen.setSize(width, height);
	}

	/**
	 * Sets the center of the screen.
	 * 
	 * @param x
	 *            X in pixels.
	 * @param y
	 *            Y in pixels.
	 */
	public void setScreenCenter(int x, int y) {
		this.screen.setScreenCenter(x, y);
	}

	/**
	 * Begin a new draw session (=> draw a new image). Sets up the gl screen
	 * assuming the current viewport is set to (0,0,width,height)
	 * 
	 * @param gl2
	 *            The gl context to use.
	 * @see #end()
	 */
	public void begin(GLDrawContext gl2) {
		this.gl = gl2;

		// beginTime = System.nanoTime();

		gl2.glPushMatrix();
		gl2.glTranslatef(-this.screen.getLeft(),
		        -this.screen.getBottom(), 0);
	}

	/**
	 * Ends a drawing session.
	 */
	public void end() {
		this.gl.glPopMatrix();
		this.gl = null;
	}

	/**
	 * Gets the current gl context, of <code>null</code> if it is called outside
	 * a gl drawing session.
	 * 
	 * @return The gl context that was given to {@link #begin(GLDrawContext)}
	 */
	public GLDrawContext getGl() {
		return this.gl;
	}

	/**
	 * Gets the x position in draw space of a map position, including height.
	 * 
	 * @param position
	 *            The position in map space.
	 * @return The x coordinate of the position in draw space.
	 */
	// public int getX(ISPosition2D position) {
	// return (int) this.converter.getViewX(position);
	// }

	/**
	 * Gets the y position in draw space of a map position, including height.
	 * <p>
	 * Faster than {@link #getY(ISPosition2D)}.
	 * 
	 * @param tile
	 *            The tile to get the position for.
	 * @return The y coordinate of its position in draw space.
	 */
	// public int getY(IHexTile tile) {
	// return (int) this.converter.getViewY(tile);
	// }

	/**
	 * Gets the y position in draw space of a map position, including height.
	 * 
	 * @param position
	 *            The position in map space.
	 * @return The y coordinate of the position in draw space.
	 */
	// public int getY(ISPosition2D position) {
	// return getY(this.map.getTile(position));
	// }

	/**
	 * Gets the region of the draw space that is drawn on the screen and
	 * therefore rendered.
	 * 
	 * @return The region displayed on the screen as Rectangle.
	 */
	public ScreenPosition getScreen() {
		return this.screen;
	}

	/**
	 * TODO: height
	 * 
	 * @param x
	 *            The x coordinate in draw space
	 * @param y
	 *            The y coordinate in draw space.
	 * @return The map position under the point.
	 */
	public ISPosition2D getPositionUnder(int x, int y) {
//		short tiley =
//		        (short) Math.round(this.map.getHeight() - (float) y
//		                / IHexTile.Y_DISTANCE);
//		float incline = IHexTile.X_DISTANCE / 2.0f / IHexTile.Y_DISTANCE;
//		short tilex =
//		        (short) Math.round((x - incline * y) / IHexTile.X_DISTANCE);
//		return new ShortPoint2D(tilex, tiley);
		return converter.getMap(x, y);
	}

	/**
	 * TODO: height
	 * 
	 * @param x
	 *            The x coordinate in screen space
	 * @param y
	 *            The y coordinate in screen space.
	 * @return The map position under the screen point.
	 */
	public ISPosition2D getPositionOnScreen(int x, int y) {
		return getPositionUnder(x + this.screen.getLeft(),
		        y + this.screen.getBottom());
	}

	/**
	 * Checks two map coordiantes if they are on the map.
	 * 
	 * @param x
	 *            The y coordinate in map space.
	 * @param y
	 *            The x coordinate in map space.
	 * @return If the map coordinates are on the map.
	 */
	public boolean checkMapCoordinates(int x, int y) {
		return x >= 0 && x < this.map.getWidth() && y >= 0
		        && y < this.map.getHeight();
	}

	/**
	 * Gets a tile.
	 * 
	 * @param x
	 *            The x coordinate in map space.
	 * @param y
	 *            The y coordinate in map space.
	 * @return The tile or <code>null</code> if no tile is on that position
	 */
	public IHexTile getTile(short x, short y) {
		if (checkMapCoordinates(x, y)) {
			return this.map.getTile(x, y);
		} else {
			return null;
		}
	}

	/**
	 * Gets a tile in a given direction next to an other tile.
	 * 
	 * @param position
	 *            The position.
	 * @param direction
	 *            THe direction.
	 * @return The tile or <code>null</code> if there is no tile in that
	 *         direction.
	 */
	public IHexTile getTileInDirection(ISPosition2D position,
	        EDirection direction) {
		short x = (short) (position.getX() + direction.getGridDeltaX());
		short y = (short) (position.getY() + direction.getGridDeltaY());

		if (checkMapCoordinates(x, y)) {
			return this.map.getTile(new ShortPoint2D(x, y));
		} else {
			return null;
		}
	}

	/**
	 * Gets the color for a given player.
	 * 
	 * @param player
	 *            The player to get the color for.
	 * @return The color.
	 */
	public Color getPlayerColor(byte player) {
		if (player >= 0) {
			return this.playerColors[player % this.playerColors.length];
		} else {
			return Color.BLACK;
		}
	}

	public void debugTime(String string) {
		// System.out.println("Draw progress: " + string + " (time: "
		// + (System.nanoTime() - beginTime) + ")");
	}

	/**
	 * Gets the converter for the map coordinate system
	 * 
	 * @return The map coordinate converter.
	 */
	public MapCoordinateConverter getConverter() {
		return this.converter;
	}

	/**
	 * sets up the gl drawing context to draw a given tile.
	 * 
	 * @param tile
	 *            The tile to draw.
	 */
	public void beginTileContext(IHexTile tile) {
		this.gl.glPushMatrix();
		this.gl.glTranslatef(this.converter.getViewX(tile),
		        this.converter.getViewY(tile), 0);
	}

	/**
	 * Assumes that the user begun drawing a tile recently, and ends drawing the
	 * tile. This also resets the view matrix to the one before starting to
	 * draw.
	 */
	public void endTileContext() {
		this.gl.glPopMatrix();
	}

	/**
	 * Gets a map tile.
	 * 
	 * @param pos
	 *            The position of the tile
	 * @return The map tile at the given position
	 * @see #getTile(short, short)
	 */
	public IHexTile getTile(ISPosition2D pos) {
		return getTile(pos.getX(), pos.getY());
	}

	/**
	 * Sets up drawing between two tiles.
	 * 
	 * @param tile
	 *            The start tile
	 * @param destination
	 *            The second tile
	 * @param progress
	 *            The progress between those two bytes.
	 */
	public void beginBetweenTileContext(IHexTile tile, IHexTile destination,
	        float progress) {
		this.gl.glPushMatrix();
		float x =
		        (1 - progress) * this.converter.getViewX(tile) + progress
		                * this.converter.getViewX(destination);
		float y =
		        (1 - progress) * this.converter.getViewY(tile) + progress
		                * this.converter.getViewY(destination);
		this.gl.glTranslatef(x, y, 0);
	}

	/**
	 * gets a rect on the screen.
	 * 
	 * @param x1
	 *            one x (not ordered)
	 * @param y1
	 *            one y
	 * @param x2
	 *            an other x
	 * @param y2
	 *            an other y
	 * @return The rectangle on the map
	 */
	public IMapArea getRectangleOnScreen(int x1, int y1, int x2, int y2) {
		int drawx1 = x1 + this.screen.getLeft();
		int drawx2 = x2 + this.screen.getLeft();
		int drawy1 = y1 + this.screen.getBottom();
		int drawy2 = y2 + this.screen.getBottom();
		return this.converter.getMapForScreen(new IntRectangle(drawx1, drawy1,
		        drawx2, drawy2));
	}

	public IMapArea getScreenArea() {
		return this.converter.getMapForScreen(this.screen.getPosition());
	}

	public void scrollTo(ISPosition2D point) {
	    int x = Math.round(converter.getViewX(point));
	    int y = Math.round(converter.getViewY(point));
	    screen.setScreenCenter(x, y);
    }
}
