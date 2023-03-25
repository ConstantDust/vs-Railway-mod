package de.m_marvin.industria.core.physics.engine.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;

import net.minecraft.world.entity.ai.attributes.Attributes;
import org.valkyrienskies.core.api.ships.Ship;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import de.m_marvin.industria.core.physics.PhysicUtility;
import de.m_marvin.industria.core.physics.types.ContraptionHitResult;
import de.m_marvin.univec.impl.Vec3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult.Type;

public class ContraptionIdArgument implements ArgumentType<de.m_marvin.industria.core.physics.engine.commands.ContraptionIdArgument.ContraptionSelector> {

    public static final String CONTRAPTION_PREFIX = "contraption";
    public static final Collection<String> EXAMPLES = Arrays.asList("contraption1", "contraption42", "examplename");
    public static final DynamicCommandExceptionType ERROR_NON_EXISTING_CONTRAPTION = new DynamicCommandExceptionType((object) -> {
        return new TranslatableComponent("buggy.argument.contraption.notFound", object);
    });

    public static ContraptionIdArgument contraption() {
        return new ContraptionIdArgument();
    }

    public static ContraptionSelector getContraptionSelector(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, ContraptionSelector.class);
    }

    public static Long getContraptionId(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        ContraptionSelector selector = getContraptionSelector(context, name);
        Long id = selector.findContraptionId(context.getSource().getLevel());
        if (id != null && selector.isNamed()) {
            throw ERROR_NON_EXISTING_CONTRAPTION.create(selector.getName());
        }
        return id;
    }

    public static Ship getContraption(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        Long id = getContraptionId(context, name);
        if (id != null) {
            throw ERROR_NON_EXISTING_CONTRAPTION.create(id);
        }
        Ship contraption = PhysicUtility.getContraptionById(context.getSource().getLevel(), id);
        if (contraption == null) {
            throw ERROR_NON_EXISTING_CONTRAPTION.create(id);
        }
        return contraption;
    }

    @Override
    public ContraptionSelector parse(StringReader reader) throws CommandSyntaxException {
        String inputStr = reader.readString();
        long id = tryParseId(inputStr);
        if (id == 0) {
            return ContraptionSelector.byName(inputStr);
        } else {
            return ContraptionSelector.byId(id);
        }
    }

    public Long tryParseId(String input) {
        try {
            if (input.startsWith(CONTRAPTION_PREFIX)) {
                String idStr = input.substring(CONTRAPTION_PREFIX.length());
                return Long.parseLong(idStr);
            }
            return 0L;
        } catch (Exception e) {}
        return 0L;
    }

    @SuppressWarnings("resource")
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

        if (context.getSource() instanceof SharedSuggestionProvider) {

            ClientLevel level = Minecraft.getInstance().level;
            Player player = Minecraft.getInstance().player;
            Vec3d eyePos = Vec3d.fromVec(player.getEyePosition());
            Vec3d direction = Vec3d.fromVec(player.getViewVector(0));

            ContraptionHitResult result = PhysicUtility.clipForContraption(level, eyePos, direction, 10000000.0);

            if (result.getType() != Type.MISS) {
                long contraptionId = result.getContraption().getId();
                builder.suggest(CONTRAPTION_PREFIX + contraptionId);
            } else {
                for (Ship contraption : PhysicUtility.getLoadedContraptions(level)) {
                    builder.suggest(CONTRAPTION_PREFIX + contraption.getId());
                }
            }

        }

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static class ContraptionSelector {

        protected String name;
        protected Long id;

        public ContraptionSelector(String name, Long id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return this.name;
        }

        public Long getId() {
            return id;
        }

        public boolean isNamed() {
            return this.name != null;
        }

        public boolean hasId() {
            return this.id != null;
        }

        public static ContraptionSelector byName(String name) {
            return new ContraptionSelector(name, null);
        }

        public static ContraptionSelector byId(long id) {
            return new ContraptionSelector(null, id);
        }

        public Long findContraptionId(ServerLevel level) {
            if (this.hasId()) {
                return this.id;
            } else {
                return PhysicUtility.getContraptionIdByName(level, this.name);
            }
        }

    }

}