package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.balance.*;
import cn.blockforge.generated.generatedmod.boss.combat.BossCombatCatalog;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Reports default source tuning, without launching a world or loading server config. */
public final class BalanceReport {
    private static final StringBuilder MARKDOWN = new StringBuilder("# Default Balance Report\n\nDefault source values; server config, gear and active buffs can change live values.\n");
    private static Path directory;
    private static void table(String name, String header, List<String> rows) throws Exception {
        Files.writeString(directory.resolve(name + ".csv"), header + "\n" + String.join("\n", rows) + "\n", StandardCharsets.UTF_8);
        MARKDOWN.append("\n## ").append(name).append("\n\n|").append(header.replace(",", "|"))
                .append("|\n|").append("---|".repeat(header.split(",").length)).append('\n');
        for (String row : rows) MARKDOWN.append('|').append(row.replace(",", "|")).append("|\n");
    }
    private static String row(Object... values) {
        return String.join(",", Arrays.stream(values).map(value -> value instanceof Double || value instanceof Float
                ? String.format(Locale.ROOT, "%.4f", ((Number)value).doubleValue()) : value.toString()).toList());
    }
    public static void main(String[] args) throws Exception {
        directory = Path.of(args.length == 0 ? "reports/balance" : args[0]);
        Files.createDirectories(directory);
        var rows = new ArrayList<String>();
        for (int realm = 0; realm < 9; realm++) rows.add(row(realm, RealmBalance.mana(realm), RealmBalance.health(realm),
                RealmBalance.attack(realm), RealmBalance.cultivation(realm, 0), RealmBalance.meditation(realm),
                RealmBalance.regeneration(realm), 10 * RealmBalance.cultivation(realm, 0) / RealmBalance.meditation(realm) / 60));
        table("realms", "realm,mana,health,attack,cultivation_base,meditation_per_second,regen_fraction,four_stage_minutes", rows);
        rows.clear();
        double min = Double.POSITIVE_INFINITY, max = 0;
        for (var art : DivineArtCatalog.ENTRIES) {
            int mana = RealmBalance.mana(art.requiredRealm());
            double fraction = DivineArtCosts.activation(art.id()) / (double)mana;
            min = Math.min(min, fraction); max = Math.max(max, fraction);
            rows.add(row(art.id(), art.requiredRealm(), DivineArtCosts.activation(art.id()), fraction,
                    DivineArtCosts.perSecond(art.id()), DivineArtCosts.perSecond(art.id()) / (double)mana, art.cooldownTicks()));
        }
        table("divine", "id,unlock_realm,activation,activation_fraction,upkeep_per_second,upkeep_fraction,cooldown_ticks", rows);
        if (max > min * 4) MARKDOWN.append("\nWARNING: divine activation fractions differ by more than 4x.\n");
        rows.clear();
        for(double distance:new double[]{0,32,64,128,256}) rows.add(row(distance,
                DivineArtCosts.soulDistanceMultiplier(distance),DivineArtCosts.maintenance("yuan_shen_chu_qiao",distance)));
        table("soul_distance_upkeep","distance_blocks,multiplier,mana_per_second",rows);
        MARKDOWN.append("\nSoul Projection's divine-table upkeep is the base at zero distance; actual upkeep grows continuously with distance from its persistent body anchor. There is no fixed range cutoff.\n");
        rows.clear();
        for (var art : ArtCatalog.ENTRIES) {
            int realm = ProgressionGate.requiredRealmForArt(art.id());
            float cost = ArtCatalog.finalManaCost(art.id(), RealmBalance.mana(realm));
            rows.add(row(art.id(), realm, ArtCatalog.fixedManaCost(art.id()), ArtCatalog.percentageCost(art.id()),
                    cost, cost / RealmBalance.mana(realm), ArtCatalog.cooldownTicks(art.id()),
                    cost/(RealmBalance.mana(realm)*RealmBalance.regeneration(realm)),ArtCatalog.damageInjectionMultiplier(art.id())));
        }
        table("arts", "id,unlock_realm,fixed_cost,percent_cost,total_cost,total_fraction,cooldown_ticks,natural_recovery_seconds,damage_injection", rows);
        rows.clear();
        for (String method : HeartMethodCatalog.METHOD_IDS) {
            rows.add(row(method,ProgressionGate.requiredRealmForHeartMethod(method),HeartMethodBalance.manaMultiplier(method),
                    HeartMethodBalance.cultivationMultiplier(method,false),HeartMethodBalance.cultivationMultiplier(method,true),
                    HeartMethodBalance.regenerationMultiplier(method,false),HeartMethodBalance.regenerationMultiplier(method,true),
                    HeartMethodBalance.healthBonus(method),HeartMethodBalance.attackBonus(method),HeartMethodBalance.speedBonus(method),
                    HeartMethodBalance.damageTakenMultiplier(method),HeartMethodBalance.breakthroughFraction(method)));
        }
        table("heart_methods","id,unlock_realm,mana_multiplier,cultivation_multiplier,meditation_cultivation_multiplier,regen_multiplier,meditation_regen_multiplier,health_bonus,attack_bonus,speed_bonus,damage_taken_multiplier,breakthrough_mana_fraction",rows);
        rows.clear();
        int[] scenarioRealms={0,2,4,7};
        String[] scenarioArts={"art_sword_aura","art_flame_wave","art_myriad_swords","art_five_elements"};
        for(int sample=0;sample<scenarioRealms.length;sample++) {
            int realm=scenarioRealms[sample];String art=scenarioArts[sample];
            double maximum=RealmBalance.mana(realm),mana=maximum;
            float cost=ArtCatalog.finalManaCost(art,(float)maximum);
            int casts=0,next=0;
            for(int tick=0;tick<1200;tick++) {
                if(tick>=next&&mana>=cost){mana-=cost;casts++;next=tick+ArtCatalog.cooldownTicks(art);}
                mana=Math.min(maximum,mana+maximum*RealmBalance.regeneration(realm)/20.0D);
            }
            double shield=4*(ArtCatalog.shieldAmplifier("art_immovable_mountain",RealmBalance.health(realm))+1);
            rows.add(row(realm,art,casts,mana-maximum,shield,shield/RealmBalance.health(realm)));
        }
        table("combat_60s_theoretical","realm,art,casts_in_60s,net_mana,heaven_shield_capacity,shield_health_fraction",rows);
        MARKDOWN.append("\nThe 60-second samples start at full base mana with one root, neutral Dao heart, no heart method, gear, kills, meditation or consumables. They model costs and cooldowns only; early samples may not have learned Heaven-tier defense. These are theoretical baselines, not measured combat/phase/kill times.\n");
        rows.clear();
        double previous = 0;
        for (int realm = 0; realm < 8; realm++) {
            int count = TribulationBalance.strikes(realm); float health = RealmBalance.health(realm); double total = 0;
            for (int strike = 1; strike <= count; strike++) total += TribulationBalance.damage(health, count, strike);
            double ratio = total / health;
            rows.add(row(realm, count, TribulationBalance.damage(health, count, 1), TribulationBalance.damage(health, count, count), total, ratio));
            if (previous > 0 && Math.abs(ratio / previous - 1) > .35) MARKDOWN.append("\nWARNING: tribulation ratio jump at realm ").append(realm).append(".\n");
            previous = ratio;
        }
        table("tribulation", "realm,strikes,first_damage,last_damage,total_damage,health_ratio", rows);
        rows.clear();
        var chances = List.of(ChestBalance.LOW, ChestBalance.MEDIUM, ChestBalance.HIGH);
        for (int i = 0; i < chances.size(); i++) {
            var chance = chances.get(i);
            rows.add(row(List.of("low", "medium", "high").get(i), chance.insight(), chance.manual(), chance.divine(), chance.divine() / 7));
        }
        table("loot", "tier,insight,manual,divine_pool,each_of_seven_divine_manuals", rows);
        rows.clear();
        MARKDOWN.append("\nBoss scenario: projected heart-sword hit = 100; player max HP = 1000; one full hit/second at 50% uptime gives 50 DPS. No healing or phase downtime; estimates are not measured kill times. Cast cap covers all hits in one cast before mitigation.\n");
        for (var boss : BossCombatCatalog.all()) {
            double hp = Math.max(160, 100 * boss.healthMultiplier());
            rows.add(row(boss.id(), boss.skills().size(), hp, 300, 50, hp / 50));
        }
        var finalBoss = cn.blockforge.generated.generatedmod.boss.combat.HeartDemonSkillPrograms.PROFILE;
        double finalHp = HeartDemonBalance.phaseOneHealth(1000) + Math.min(5_000_000, Math.max(200, 100 * finalBoss.healthMultiplier()));
        rows.add(row(finalBoss.id(), finalBoss.skills().size(), finalHp, 300, 50, finalHp / 50));
        table("bosses", "id,skills,scenario_hp,cast_damage_cap,reference_dps,estimated_seconds", rows);
        MARKDOWN.append("\nFinal Heart Demon phase one mirrors player HP at x"+HeartDemonBalance.HEALTH_MULTIPLIER
                +"; scenario phase-one HP = "+HeartDemonBalance.phaseOneHealth(1000)
                +". The total adds phase two's heart-sword scaling, before copied effects. Acquisition reachability and measured TPS/MSPT/network usage are separate audits; this report does not infer them from drop rates.\n");
        Files.writeString(directory.resolve("balance.md"), MARKDOWN, StandardCharsets.UTF_8);
        System.out.println("Balance report: " + directory.toAbsolutePath());
    }
}
