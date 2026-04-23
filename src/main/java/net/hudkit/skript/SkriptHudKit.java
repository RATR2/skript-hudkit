package net.hudkit.skript;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.util.Kleenean;
import net.hudkit.api.Hud;
import net.hudkit.api.HudManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;

import javax.annotation.Nullable;

public final class SkriptHudKit extends JavaPlugin {

    // ── Plugin singleton ──────────────────────────────────────────────

    private static SkriptHudKit instance;
    private static SkriptAddon addonInstance;

    public static SkriptHudKit getInstance() {
        return instance;
    }

    public static SkriptAddon getAddonInstance() {
        return addonInstance;
    }

    // ── HudManager access ─────────────────────────────────────────────

    @Nullable
    public static HudManager getHudManager() {
        if (instance == null) return null;

        Plugin hudkitPlugin = instance.getServer().getPluginManager().getPlugin("Hudkit");
        if (hudkitPlugin == null || !hudkitPlugin.isEnabled()) return null;

        try {
            var getter = hudkitPlugin.getClass().getMethod("getHudManager");
            if (!HudManager.class.isAssignableFrom(getter.getReturnType())) return null;

            Object manager = getter.invoke(hudkitPlugin);
            return manager instanceof HudManager hudManager ? hudManager : null;
        } catch (ReflectiveOperationException | SecurityException ignored) {
            return null;
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────

    @Override
    public void onEnable() {
        instance = this;

        addonInstance = ch.njol.skript.Skript.instance().registerAddon(
            SkriptHudKit.class, "SkriptHudKit");

        SyntaxRegistry registry = addonInstance.syntaxRegistry();

        // ── Effects ───────────────────────────────────────────────────
        registry.register(SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffShowHudElement.class)
                        .addPatterns("(show|display) hud element %string% for %player%")
                        .build());

        registry.register(SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffHideHudElement.class)
                        .addPatterns("hide hud element %string% for %player%")
                        .build());

        registry.register(SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffSetHudState.class)
                        .addPatterns("set hud state %string% to %string% for %player%")
                        .build());

        registry.register(SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffShowHud.class)
                        .addPatterns("(show|display) hud for %player%")
                        .build());

        registry.register(SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffHideHud.class)
                        .addPatterns("hide hud for %player%")
                        .build());

        registry.register(SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffClearHudState.class)
                        .addPatterns("clear hud state %string% for %player%")
                        .build());

        registry.register(SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffRerenderHud.class)
                        .addPatterns("rerender hud for %player%")
                        .build());

        registry.register(SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffRerenderHuds.class)
                        .addPatterns("rerender [all] hud[s]")
                        .build());

        // ── Expressions ───────────────────────────────────────────────
        registry.register(SyntaxRegistry.EXPRESSION,
                DefaultSyntaxInfos.Expression.builder(ExprHudOfPlayer.class, Hud.class)
                        .addPatterns(
                                "[the] hud of %player%",
                                "%player%'s hud")
                        .build());

        // ── Conditions ────────────────────────────────────────────────
        registry.register(SyntaxRegistry.CONDITION,
                SyntaxInfo.builder(CondHasActiveHud.class)
                        .addPatterns(
                                "%player% has [an] active hud",
                                "%player% (doesn't|does not) have [an] active hud")
                        .build());

        getLogger().info("SkriptHudKit enabled successfully (Skript 2.14+ / HudKit 1.21.11+).");
    }

    @Override
    public void onDisable() {
        getLogger().info("SkriptHudKit disabled.");
    }

    // ══════════════════════════════════════════════════════════════════
    //  EFFECTS
    // ══════════════════════════════════════════════════════════════════

    // ── show hud element "key" for <player> ───────────────────────────

    @Name("Show HUD Element")
    @Description("Makes a specific HUD element visible for a player.")
    @Examples("show hud element \"health_bar\" for player")
    @Since("1.0.0")
    public static final class EffShowHudElement extends Effect {

        private Expression<String> elementKey;
        private Expression<Player> target;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] exprs, int matchedPattern,
                            Kleenean isDelayed, ParseResult parseResult) {
            elementKey = (Expression<String>) exprs[0];
            target     = (Expression<Player>) exprs[1];
            return true;
        }

        @Override
        protected void execute(Event event) {
            String key    = elementKey.getSingle(event);
            Player player = target.getSingle(event);
            if (key == null || player == null) return;

            HudManager manager = getHudManager();
            if (manager == null) return;

            Hud hud = manager.getHud(player);
            if (hud == null) return;

            hud.show(key);
        }

        @Override
        public String toString(@Nullable Event event, boolean debug) {
            return "show hud element " + elementKey.toString(event, debug)
                    + " for " + target.toString(event, debug);
        }
    }

    // ── hide hud element "key" for <player> ───────────────────────────

    @Name("Hide HUD Element")
    @Description("Hides a specific HUD element for a player.")
    @Examples("hide hud element \"health_bar\" for player")
    @Since("1.0.0")
    public static final class EffHideHudElement extends Effect {

        private Expression<String> elementKey;
        private Expression<Player> target;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] exprs, int matchedPattern,
                            Kleenean isDelayed, ParseResult parseResult) {
            elementKey = (Expression<String>) exprs[0];
            target     = (Expression<Player>) exprs[1];
            return true;
        }

        @Override
        protected void execute(Event event) {
            String key    = elementKey.getSingle(event);
            Player player = target.getSingle(event);
            if (key == null || player == null) return;

            HudManager manager = getHudManager();
            if (manager == null) return;

            Hud hud = manager.getHud(player);
            if (hud == null) return;

            hud.hide(key);
        }

        @Override
        public String toString(@Nullable Event event, boolean debug) {
            return "hide hud element " + elementKey.toString(event, debug)
                    + " for " + target.toString(event, debug);
        }
    }

    // ── set hud state "key" to "value" for <player> ───────────────────

    @Name("Set HUD State")
    @Description("Sets a dynamic state value on a player's HUD element (e.g. for image state machines).")
    @Examples("set hud state \"mode\" to \"combat\" for player")
    @Since("1.0.0")
    public static final class EffSetHudState extends Effect {

        private Expression<String> stateKey;
        private Expression<String> stateValue;
        private Expression<Player> target;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] exprs, int matchedPattern,
                            Kleenean isDelayed, ParseResult parseResult) {
            stateKey   = (Expression<String>) exprs[0];
            stateValue = (Expression<String>) exprs[1];
            target     = (Expression<Player>) exprs[2];
            return true;
        }

        @Override
        protected void execute(Event event) {
            String key    = stateKey.getSingle(event);
            String value  = stateValue.getSingle(event);
            Player player = target.getSingle(event);
            if (key == null || value == null || player == null) return;

            HudManager manager = getHudManager();
            if (manager == null) return;

            Hud hud = manager.getHud(player);
            if (hud == null) return;

            hud.setState(key, value);
        }

        @Override
        public String toString(@Nullable Event event, boolean debug) {
            return "set hud state " + stateKey.toString(event, debug)
                    + " to " + stateValue.toString(event, debug)
                    + " for " + target.toString(event, debug);
        }
    }

    // ── show hud for <player> ─────────────────────────────────────────

    @Name("Show HUD")
    @Description("Attaches and shows the full HUD for a player.")
    @Examples("show hud for player")
    @Since("1.0.0")
    public static final class EffShowHud extends Effect {

        private Expression<Player> target;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] exprs, int matchedPattern,
                            Kleenean isDelayed, ParseResult parseResult) {
            target = (Expression<Player>) exprs[0];
            return true;
        }

        @Override
        protected void execute(Event event) {
            Player player = target.getSingle(event);
            if (player == null) return;

            HudManager manager = getHudManager();
            if (manager == null) return;

            manager.showHud(player);
        }

        @Override
        public String toString(@Nullable Event event, boolean debug) {
            return "show hud for " + target.toString(event, debug);
        }
    }

    // ── hide hud for <player> ─────────────────────────────────────────

    @Name("Hide HUD")
    @Description("Removes the entire HUD from a player (hides the bossbar and cleans up).")
    @Examples("hide hud for player")
    @Since("1.0.0")
    public static final class EffHideHud extends Effect {

        private Expression<Player> target;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] exprs, int matchedPattern,
                            Kleenean isDelayed, ParseResult parseResult) {
            target = (Expression<Player>) exprs[0];
            return true;
        }

        @Override
        protected void execute(Event event) {
            Player player = target.getSingle(event);
            if (player == null) return;

            HudManager manager = getHudManager();
            if (manager == null) return;
            Hud hud = manager.getHud(player);
            if (hud != null) {
                hud.hideHud();
            }
            manager.removeHud(player);
        }

        @Override
        public String toString(@Nullable Event event, boolean debug) {
            return "hide hud for " + target.toString(event, debug);
        }
    }

    // ── clear hud state "key" for <player> ───────────────────────────

    @Name("Clear HUD State")
    @Description("Clears the state of a dynamic image element for a player, reverting it to its default state.")
    @Examples("clear hud state \"mode\" for player")
    @Since("1.0.0")
    public static final class EffClearHudState extends Effect {

        private Expression<String> stateKey;
        private Expression<Player> target;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] exprs, int matchedPattern,
                            Kleenean isDelayed, ParseResult parseResult) {
            stateKey = (Expression<String>) exprs[0];
            target   = (Expression<Player>) exprs[1];
            return true;
        }

        @Override
        protected void execute(Event event) {
            String key    = stateKey.getSingle(event);
            Player player = target.getSingle(event);
            if (key == null || player == null) return;

            HudManager manager = getHudManager();
            if (manager == null) return;

            Hud hud = manager.getHud(player);
            if (hud == null) return;

            hud.clearState(key);
        }

        @Override
        public String toString(@Nullable Event event, boolean debug) {
            return "clear hud state " + stateKey.toString(event, debug)
                    + " for " + target.toString(event, debug);
        }
    }

    // ── rerender hud for <player> ─────────────────────────────────────

    @Name("Rerender HUD")
    @Description("Forces an immediate rerender of a specific player's HUD.")
    @Examples("rerender hud for player")
    @Since("1.0.0")
    public static final class EffRerenderHud extends Effect {

        private Expression<Player> target;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] exprs, int matchedPattern,
                            Kleenean isDelayed, ParseResult parseResult) {
            target = (Expression<Player>) exprs[0];
            return true;
        }

        @Override
        protected void execute(Event event) {
            Player player = target.getSingle(event);
            if (player == null) return;

            HudManager manager = getHudManager();
            if (manager == null) return;

            Hud hud = manager.getHud(player);
            if (hud == null) return;

            hud.rerender();
        }

        @Override
        public String toString(@Nullable Event event, boolean debug) {
            return "rerender hud for " + target.toString(event, debug);
        }
    }

    // ── rerender all huds ─────────────────────────────────────────────

    @Name("Rerender All HUDs")
    @Description("Forces an immediate rerender of every active player HUD.")
    @Examples("rerender all huds")
    @Since("1.0.0")
    public static final class EffRerenderHuds extends Effect {

        @Override
        public boolean init(Expression<?>[] exprs, int matchedPattern,
                            Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        protected void execute(Event event) {
            HudManager manager = getHudManager();
            if (manager == null) return;
            manager.rerenderAllHuds();
        }

        @Override
        public String toString(@Nullable Event event, boolean debug) {
            return "rerender all huds";
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  EXPRESSIONS
    // ══════════════════════════════════════════════════════════════════

    // ── [the] hud of <player> / <player>'s hud ────────────────────────

    @Name("HUD of Player")
    @Description("Returns the Hud object currently active for a player, or nothing if they have no active HUD.")
    @Examples({
        "set {_hud} to the hud of player",
        "if player's hud is set:"
    })
    @Since("1.0.0")
    public static final class ExprHudOfPlayer extends SimpleExpression<Hud> {

        private Expression<Player> target;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] exprs, int matchedPattern,
                            Kleenean isDelayed, ParseResult parseResult) {
            target = (Expression<Player>) exprs[0];
            return true;
        }

        @Override
        @Nullable
        protected Hud[] get(Event event) {
            Player player = target.getSingle(event);
            if (player == null) return null;

            HudManager manager = getHudManager();
            if (manager == null) return null;

            Hud hud = manager.getHud(player);
            return hud != null ? new Hud[]{hud} : null;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Hud> getReturnType() {
            return Hud.class;
        }

        @Override
        public String toString(@Nullable Event event, boolean debug) {
            return "hud of " + target.toString(event, debug);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  CONDITIONS
    // ══════════════════════════════════════════════════════════════════

    // ── <player> has [an] active hud ──────────────────────────────────

    @Name("Has Active HUD")
    @Description("Checks whether a player currently has an active HudKit HUD.")
    @Examples({
        "if player has an active hud:",
        "    hide hud element \"compass\" for player"
    })
    @Since("1.0.0")
    public static final class CondHasActiveHud extends ch.njol.skript.lang.Condition {

        private Expression<Player> target;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] exprs, int matchedPattern,
                            Kleenean isDelayed, ParseResult parseResult) {
            target = (Expression<Player>) exprs[0];
            setNegated(matchedPattern == 1);
            return true;
        }

        @Override
        public boolean check(Event event) {
            Player player = target.getSingle(event);
            if (player == null) return isNegated();

            HudManager manager = getHudManager();
            if (manager == null) return isNegated();

            boolean hasHud = manager.getHud(player) != null;
            return isNegated() != hasHud;
        }

        @Override
        public String toString(@Nullable Event event, boolean debug) {
            return target.toString(event, debug)
                    + (isNegated() ? " does not have" : " has")
                    + " an active hud";
        }
    }
}