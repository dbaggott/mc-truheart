package io.dnbg.minecraft.actualstats.client.mixin;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the private {@code exhaustionLevel} field on {@link FoodData} so
 * the HUD can render it. Vanilla only exposes the setter
 * ({@code addExhaustion}); the value itself is invisible from outside the
 * class. This is the canonical Fabric way to widen access without forking
 * vanilla code — at runtime Mixin generates a getter implementation, and
 * we cast a {@link FoodData} instance to this interface to call it.
 *
 * <p>Usage:
 * <pre>
 *   float exh = ((FoodDataAccessor)(Object) food).getExhaustionLevel();
 * </pre>
 * The intermediate {@code (Object)} cast is required because the compiler
 * doesn't know {@link FoodData} implements this interface — Mixin attaches
 * it at class-load time.
 */
@Mixin(FoodData.class)
public interface FoodDataAccessor {
	@Accessor("exhaustionLevel")
	float getExhaustionLevel();
}
