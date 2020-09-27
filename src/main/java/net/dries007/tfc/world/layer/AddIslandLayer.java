/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.world.layer;

import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.layer.traits.IBishopTransformer;

public enum AddIslandLayer implements IBishopTransformer
{
    NORMAL(5),
    HEAVY(12);

    private final int oceanWeight;

    AddIslandLayer(int oceanWeight)
    {
        this.oceanWeight = oceanWeight;
    }

    public int apply(INoiseRandom context, int bottomLeft, int bottomRight, int topRight, int topLeft, int center)
    {
        if (!TFCLayerUtil.isOcean(center) || (TFCLayerUtil.isOcean(bottomLeft) && TFCLayerUtil.isOcean(bottomRight) && TFCLayerUtil.isOcean(topLeft) && TFCLayerUtil.isOcean(topRight)))
        {
            if (context.nextRandom(oceanWeight) == 0)
            {
                if (TFCLayerUtil.isOcean(topLeft))
                {
                    return topLeft;
                }
                if (TFCLayerUtil.isOcean(topRight))
                {
                    return topRight;
                }
                if (TFCLayerUtil.isOcean(bottomLeft))
                {
                    return bottomLeft;
                }
                if (TFCLayerUtil.isOcean(bottomRight))
                {
                    return bottomRight;
                }
            }
            return center;
        }
        int counter = 1;
        int replacement = 1;
        if (!TFCLayerUtil.isOcean(topLeft) && context.nextRandom(counter++) == 0)
        {
            replacement = topLeft;
        }

        if (!TFCLayerUtil.isOcean(topRight) && context.nextRandom(counter++) == 0)
        {
            replacement = topRight;
        }

        if (!TFCLayerUtil.isOcean(bottomLeft) && context.nextRandom(counter++) == 0)
        {
            replacement = bottomLeft;
        }

        if (!TFCLayerUtil.isOcean(bottomRight) && context.nextRandom(counter) == 0)
        {
            replacement = bottomRight;
        }

        if (context.nextRandom(3) == 0)
        {
            return replacement;
        }
        else
        {
            return center;
        }
    }
}
