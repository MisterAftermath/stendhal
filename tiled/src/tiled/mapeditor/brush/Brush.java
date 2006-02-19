/*
 *  Tiled Map Editor, (c) 2004
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 * 
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <b.lindeijer@xs4all.nl>
 *  
 *  modified for stendhal, an Arianne powered RPG 
 *  (http://arianne.sf.net)
 *
 *  Matthias Totz <mtotz@users.sourceforge.net>
 */

package tiled.mapeditor.brush;

import java.awt.Graphics;
import java.awt.Rectangle;

import tiled.core.MapLayer;
import tiled.core.MultilayerPlane;

/** A brush */
public interface Brush 
{
  /** Returns the affected layers. If the returned array is empty
   * (length == 0) then this brush will not paint on a specific layer but on the
   * current selected one.
   * @return the layers of this brush or an empty array if the brush is
   *         layer independent.
   */
  public MapLayer[] getAffectedLayers();
  
  /** returns the size in tiles of the brush
   * @return size if the brush
   */
  public Rectangle getBounds();
  
  /**
   * This is the main processing method for a Brush object. Painting starts
   * on initLayer, and if the brush has more than one layer, then the brush  
   * will paint deeper into the layer stack.
   * 
   * @see MultilayerPlane
   * @param mp         The MultilayerPlane to be affected
   * @param x          The x-coordinate where the user initiated the paint
   * @param y          The y-coordinate where the user initiated the paint
   * @param initLayer  The first layer to paint to.
   * @return The rectangular region affected by the painting  
   */
  public Rectangle commitPaint(MultilayerPlane mp, int x, int y,int initLayer);
  
  public void paint(Graphics g, int x, int y);
  
  public boolean equals(Brush b);
}
