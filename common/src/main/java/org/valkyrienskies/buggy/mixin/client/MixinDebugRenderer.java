package org.valkyrienskies.buggy.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.m_marvin.univec.impl.Vec3d;
import de.m_marvin.univec.impl.Vec3f;
import de.m_marvin.univec.impl.Vec4i;
import kotlin.Triple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.util.Tuple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.buggy.BuggyDebugHelper;
import org.valkyrienskies.buggy.api.debug.DebugLine;
import org.valkyrienskies.buggy.api.debug.DebugObject;

import java.awt.*;

@Mixin(DebugRenderer.class)
public class MixinDebugRenderer {

    @Inject(method = "render", at = @At("HEAD"))
    private void postRender(final PoseStack matricesIgnore, final MultiBufferSource.BufferSource vertexConsumersIgnore,
                            final double cameraX, final double cameraY, final double cameraZ, final CallbackInfo ci) {
        final MultiBufferSource.BufferSource bufferSource =
                MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            for (Triple<Vec3d, Vec3d, Vec4i> line : BuggyDebugHelper.Companion.queryLines()) {
                VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());

                Vec3d cam = new Vec3d(cameraX, cameraY, cameraZ);

                Vec3d A = line.getFirst().sub(cam);
                Vec3d B = line.getSecond().sub(cam);
                Vec4i Color = line.getThird();
                Vec3f normal = new Vec3f(A.sub(B).normalize());

                vertexConsumer.vertex(A.x, A.y, A.z).color(Color.x, Color.y, Color.z, Color.w).normal(normal.x, normal.y, normal.z).endVertex();
                vertexConsumer.vertex(B.x, B.y, B.z).color(Color.x, Color.y, Color.z, Color.w).normal(normal.x, normal.y, normal.z).endVertex();
            }
            bufferSource.endBatch();
        }
    }

}
