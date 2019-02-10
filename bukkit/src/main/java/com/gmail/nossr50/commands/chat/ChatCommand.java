package com.gmail.nossr50.commands.chat;

import com.gmail.nossr50.core.chat.ChatManager;
import com.gmail.nossr50.core.chat.ChatManagerFactory;
import com.gmail.nossr50.core.config.Config;
import com.gmail.nossr50.core.data.UserManager;
import com.gmail.nossr50.core.datatypes.chat.ChatMode;
import com.gmail.nossr50.core.datatypes.party.PartyFeature;
import com.gmail.nossr50.core.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.core.locale.LocaleLoader;
import com.gmail.nossr50.core.util.commands.CommandUtils;
import com.gmail.nossr50.mcMMO;
import com.google.common.collect.ImmutableList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class ChatCommand implements TabExecutor {
    protected ChatManager chatManager;
    private ChatMode chatMode;

    public ChatCommand(ChatMode chatMode) {
        this.chatMode = chatMode;
        this.chatManager = ChatManagerFactory.getChatManager(mcMMO.p, chatMode);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        McMMOPlayer mcMMOPlayer;

        switch (args.length) {
            case 0:
                if (CommandUtils.noConsoleUsage(sender)) {
                    return true;
                }

                if (!CommandUtils.hasPlayerDataKey(sender)) {
                    return true;
                }

                mcMMOPlayer = UserManager.getPlayer(sender.getName());

                if (mcMMOPlayer.isChatEnabled(chatMode)) {
                    disableChatMode(mcMMOPlayer, sender);
                } else {
                    enableChatMode(mcMMOPlayer, sender);
                }

                return true;

            case 1:
                if (!CommandUtils.hasPlayerDataKey(sender)) {
                    return true;
                }

                if (CommandUtils.shouldEnableToggle(args[0])) {
                    if (CommandUtils.noConsoleUsage(sender)) {
                        return true;
                    }

                    enableChatMode(UserManager.getPlayer(sender.getName()), sender);
                    return true;
                }

                if (CommandUtils.shouldDisableToggle(args[0])) {
                    if (CommandUtils.noConsoleUsage(sender)) {
                        return true;
                    }

                    disableChatMode(UserManager.getPlayer(sender.getName()), sender);
                    return true;
                }

                // Fallthrough

            default:
                handleChatSending(sender, args);
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 1:
                return StringUtil.copyPartialMatches(args[0], CommandUtils.TRUE_FALSE_OPTIONS, new ArrayList<String>(CommandUtils.TRUE_FALSE_OPTIONS.size()));
            default:
                return ImmutableList.of();
        }
    }

    protected String buildChatMessage(String[] args, int index) {
        StringBuilder builder = new StringBuilder();
        builder.append(args[index]);

        for (int i = index + 1; i < args.length; i++) {
            builder.append(" ");
            builder.append(args[i]);
        }

        return builder.toString();
    }

    protected String getDisplayName(CommandSender sender) {
        return (sender instanceof Player) ? ((Player) sender).getDisplayName() : LocaleLoader.getString("Commands.Chat.Console");
    }

    protected abstract void handleChatSending(CommandSender sender, String[] args);

    private void enableChatMode(McMMOPlayer mcMMOPlayer, CommandSender sender) {
        if (chatMode == ChatMode.PARTY && mcMMOPlayer.getParty() == null) {
            sender.sendMessage(LocaleLoader.getString("Commands.Party.None"));
            return;
        }

        if (chatMode == ChatMode.PARTY && (mcMMOPlayer.getParty().getLevel() < Config.getInstance().getPartyFeatureUnlockLevel(PartyFeature.CHAT))) {
            sender.sendMessage(LocaleLoader.getString("Party.Feature.Disabled.1"));
            return;
        }

        mcMMOPlayer.enableChat(chatMode);
        sender.sendMessage(chatMode.getEnabledMessage());
    }

    private void disableChatMode(McMMOPlayer mcMMOPlayer, CommandSender sender) {
        if (chatMode == ChatMode.PARTY && mcMMOPlayer.getParty() == null) {
            sender.sendMessage(LocaleLoader.getString("Commands.Party.None"));
            return;
        }

        mcMMOPlayer.disableChat(chatMode);
        sender.sendMessage(chatMode.getDisabledMessage());
    }
}
