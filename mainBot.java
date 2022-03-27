import com.mongodb.DB;
import com.mongodb.MongoClient;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.LoginException;
import java.awt.Color;
import java.io.*;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class mainBot extends ListenerAdapter
{
    public String prefix = "**";

    public static void main(String[] args) throws LoginException {

        String token = "OTI4MDIyNzY2OTExOTE0MDU0.YdSujQ.ntf_wbey9sU3RjHxeLzBryaRQCY";
        JDABuilder builder = JDABuilder.createDefault(token, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.DIRECT_MESSAGE_TYPING);

        builder.addEventListeners(new mainBot());
        // Disable parts of the cache
        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        // Enable the bulk delete event
        builder.setBulkDeleteSplittingEnabled(false);
        // Disable compression (not recommended)
        builder.setCompression(Compression.NONE);
        // Set activity (like "playing Something")
        builder.setActivity(Activity.playing("Custom Auctions!"));
        //lets bot see members
        builder.setChunkingFilter(ChunkingFilter.ALL);
        //stores members
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        //perm to see members
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.build();

    }
    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("CustomAuctions is online!");

    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {

        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();
        String userID = event.getAuthor().getId();
        Member sender = event.getGuild().getMemberById(userID);
        classes commands = new classes();
        List<Role> roles = guild.getRolesByName("AuctionAdmin", true);

        if(event.getMessage().getContentRaw().toLowerCase().startsWith(prefix+"help"))
        {
            //TODO Command List
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Command List", null);
            eb.setColor(new Color(0xE2BB46));
            eb.setDescription("A full list of valid commands");
            eb.addField("(prefix)bid (amount) {name}", " allows you to bid on specific auction", false);
            eb.addField("(prefix)auc {name}", " displays auction information", false);
            eb.setFooter("Created by: Platform40");
            eb.build();
            channel.sendMessage(eb.build()).queue();
        }
        if(event.getMessage().getContentRaw().toLowerCase().startsWith(prefix+"admin"))
        {

            if(sender.getRoles().contains(roles.get(0)))
            {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Command List", null);
                eb.setColor(new Color(0xE2BB46));
                eb.setDescription("A full list of admin commands");
                eb.addField("(prefix)createauc (name) {starting bid} (time in minutes)", " Creates auction", false);
                eb.addField("(prefix)endauc (name)", " ends the specified auction", false);
                eb.addField("(prefix)editauc (name)", " SYNTAX WIP. allows editing of specified auction", false);
                eb.addField("(prefix)giveaucperms (@user)", " Give auction admin perm role", false);
                eb.setFooter("Created by: Platform40");
                eb.build();
                channel.sendMessage(eb.build()).queue();
            }
            else
            {
                channel.sendMessage("Incorrect Permissions").queue();
            }
        }
        if(event.getMessage().getContentRaw().toLowerCase().startsWith(prefix+"createauc"))
        {

            if(sender.getRoles().contains(roles.get(0))) {
                String m = event.getMessage().getContentRaw();
                TextChannel aucChannel = event.getGuild().getTextChannelsByName("auctions",true).get(0);
                String[] getArgs = m.split(" ");
                if (getArgs.length <= 3) {
                    channel.sendMessage("Too few args (see (prefix)admin)").queue();
                } else if (getArgs.length == 4) {
                    for (String i : getArgs) {
                        System.out.println(i);
                    }
                    String m1 = getArgs[1];//Name
                    String m2 = getArgs[2];//Starting Bid
                    String m3 = getArgs[3];//Time in minutes

                    //TODO code this
                    // The delay period is calculated in milliseconds iirc
                    final int[] _m3 = {Integer.parseInt(m3)};

                        TimerTask task = new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                System.out.println("Auction ticker");
                                String[] info = commands.getInfo(m1);
                                if (commands.incrementTime(m1, _m3[0]) == 0) {

                                    //Auction over

                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setTitle("Auction Ended: ", m1);
                                    eb.setColor(new Color(000000));
                                    eb.addField("Top Bidder: ", " <@" + info[3] + ">", false);
                                    eb.addField("Top Bid: ", info[0]+" credits", false);
                                    eb.setFooter("Created by: Platform40");
                                    eb.build();
                                    aucChannel.sendMessage(eb.build()).queue();
                                    aucChannel.sendMessage("<@"+info[3]+">");
                                    File f = new File(m1+".json");
                                    if (f.delete())
                                    {
                                        System.out.println("Deleted Auction File: " + m1+".json");
                                    } else
                                    {
                                        System.out.println("Failed to delete the file");
                                    }

                                }
                                else
                                {
                                    _m3[0]--;
                                }
                            }
                        };
                        //TODO Fix timer not working for amounts greater than 1

                        Timer timer = new Timer();
                        timer.scheduleAtFixedRate(task, 60000, 60000);

                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setColor(new Color(000000));
                    eb.setAuthor("Custom Auctions", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                    eb.setTitle("New Auction: "+m1);
                    eb.addField("Starting bid: ", m2+" credits", false);
                    eb.addField("Duration: ", m3+" minutes", false);
                    eb.build();
                    aucChannel.sendMessage(eb.build()).queue();

                    commands.newAuction(m1,m2,m3);
                } else {
                    channel.sendMessage("Too many args (see (prefix)admin)").queue();
                }
            }
            else
            {
                channel.sendMessage("Invalid Permissions +<@"+userID+">");
            }

        }
        if(event.getMessage().getContentRaw().toLowerCase().startsWith(prefix+"auc"))
        {
            String m = event.getMessage().getContentRaw();
            String[] getArgs = m.split(" ");
            if(getArgs.length>2)
            {
                //error
            }
            else if(getArgs.length==2)
            {
                //Show specified
                m = getArgs[1];
                String[] AucInfo = commands.getInfo(m);
                try
                {
                    Integer.parseInt(AucInfo[0]);

                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setColor(new Color(0xE2BB46));
                    eb.setAuthor("CustomAuctions", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                    eb.setTitle(m);
                    eb.addField("Current Bid: ", AucInfo[0] + " Credits", false);
                    eb.addField("Time Remaining: ", AucInfo[1] + " Minutes", false);
                    eb.addField("Total Bids: ", AucInfo[2], false);
                    if(!(Integer.parseInt(AucInfo[2])==0))
                    {
                        eb.addField("Top Bidder: ", "<@"+AucInfo[3]+">", true);
                    }

                    eb.build();
                    channel.sendMessage(eb.build()).queue();
                }
                catch (NumberFormatException a)
                {
                    channel.sendMessage(AucInfo[0]).queue();
                }


            }
            else
            {
                //Show all auctions
            }

        }
        if(event.getMessage().getContentRaw().toLowerCase().startsWith(prefix+"bid"))
        {
            String m = event.getMessage().getContentRaw();
            String[] getArgs = m.split(" ");

            if(getArgs.length==3)
            {
                System.out.println(getArgs[0]+ " "+ getArgs[1]+ " "+getArgs[2]);
                String name = getArgs[1];
                String bid = getArgs[2];
                String[] aucInfo = commands.getInfo(name);
                if(aucInfo[0].equals("File couldnt be found"))
                {
                    //Error auction doesnt exist
                }
                else
                {
                    if(Integer.parseInt(aucInfo[0])>Integer.parseInt(bid))
                    {
                        //error must bid more than current bid
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setColor(new Color(000000));
                        eb.setAuthor("Custom Auctions", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                        eb.addField("Error: ", "Bid Lower than current bid", false);
                        eb.build();
                        channel.sendMessage(eb.build()).queue();
                        System.out.println("loop 1");
                    }
                    else if(Integer.parseInt(aucInfo[0])*1.01>=Integer.parseInt(bid))
                    {
                        //error thats annoying
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setColor(new Color(000000));
                        eb.setAuthor("Custom Auctions", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                        eb.addField("Error: ", "Bid must be atleast 1% higher than current bid", false);
                        eb.build();
                        channel.sendMessage(eb.build()).queue();
                        System.out.println("loop 2");
                    }
                    else
                    {
                        System.out.println(commands.incrementBid(name, Integer.parseInt(bid), event.getMessage().getAuthor().getId()));
                        event.getMessage().addReaction("U+2705").queue();
                    }
                }
            }
            else
            {
                //error too many or too few args
            }

        }

    }
}
