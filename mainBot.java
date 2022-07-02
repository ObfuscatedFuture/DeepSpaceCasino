import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.LoginException;

import java.awt.Color;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class mainBot extends ListenerAdapter {

    JSONParser jsonParser = new JSONParser();
    classData accounts = new classData();
    public String authorID = " ";
    public String credits = "";
    public String receiverString = "";

    public static void main(String[] args) throws LoginException {

        String token = "REDACTED";
        JDABuilder builder = JDABuilder.createDefault(token, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.DIRECT_MESSAGE_TYPING);

        builder.addEventListeners(new mainBot());
        // Disable parts of the cache
        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        // Enable the bulk delete event
        builder.setBulkDeleteSplittingEnabled(false);
        // Disable compression (not recommended)
        builder.setCompression(Compression.NONE);
        // Set activity (like "playing Something")
        builder.setActivity(Activity.playing("Starscape"));

        builder.build();

    }

    //TODO add anti-spam reminder for DM (Typing...)
    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("DeepSpaceCasino is online!");

    }
    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event)
    {

    }
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        //Logs in console any user messages
        boolean admin = false;
        //TODO make the bot channel specific + config file
        System.out.println("Message received from user: " +
                event.getAuthor().getName() + ": " +
                event.getMessage().getContentDisplay()

        );

        MessageChannel channel = event.getChannel();
        String userProfile = event.getAuthor().getId();
        Member sender = event.getGuild().getMemberById(userProfile);
        Guild guild = event.getGuild();
        List<Role> roles = guild.getRolesByName("CasinoAdmin", true);
        List<Member> member = guild.getMembersWithRoles(roles);
        if(member.contains(sender))
        {
            admin = true;
        }
        if (event.getMessage().getContentRaw().toLowerCase().startsWith("ithelp")) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Command List", null);
            eb.setColor(new Color(0xE2BB46));
            eb.setDescription("A full list of valid commands");
            eb.addField("itnew:", " creates account for new users", false);
            eb.addField("itcredits:", " displays current credit count", false);
            eb.addField("ithf (amount) {tails/heads}:", " headflips credits", false);
            eb.addField("itlower (number 1-100) (amount):", " casino game, if the random number is lower then the number you chose you win", false);
            eb.addField("itcrates (args o | i | crate ID) {quantity of crates to buy 1-9}:", " Buy crates and crack 'em open", false);
            eb.addField("itsend (amount) (user):", " send credits from your account to another user", false);
            eb.setFooter("Created by: Platform40");
            eb.build();
            channel.sendMessageEmbeds(eb.build()).queue();
        }
        if (event.getMessage().getContentRaw().toLowerCase().startsWith("itadmin"))
        {
            if(admin)
            {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Command List", null);
                eb.setColor(new Color(0xE2BB46));
                eb.setDescription("A list of available admin commands");
                eb.addField("ituserinfo", " outputs sender's file to console", false);
                eb.addField("itadd", " allows admins to add and remove funds from account", false);
                //TODO allow admins to change config file
                eb.setFooter("Created by: Platform40");
                eb.build();
                channel.sendMessageEmbeds(eb.build()).queue();
            }
            else
            {
                channel.sendMessage("Lacking permissions").queue();
            }


        }
        if (event.getMessage().getContentRaw().toLowerCase().startsWith("ituserinfo")) {
            try (FileReader reader = new FileReader(userProfile + ".json")) {

                //Read JSON file
                System.out.println(jsonParser.parse(reader));
                System.out.println(userProfile);
                System.out.println(accounts.getCredits(userProfile));
                int[] crates = accounts.getCrates(userProfile);
                System.out.println(crates[0]+ " " +crates[1]+ " "+ crates[2]);
                System.out.println(event.getAuthor().getId());

            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
        if (event.getMessage().getContentRaw().toLowerCase().startsWith("itcredits")) {
            try (FileReader reader = new FileReader(userProfile + ".json")) {
                //Read JSON file
                String credits = accounts.getCredits(userProfile);
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xE2BB46));
                eb.setAuthor("Deep Space Casino", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                eb.addField("You have: ", credits + " Credits", false);
                eb.build();
                channel.sendMessageEmbeds(eb.build()).queue();
            } catch (FileNotFoundException e) {
                channel.sendMessage(event.getAuthor().getAsMention() + " Couldn't find your account create one with itnew").queue();
            } catch (IndexOutOfBoundsException | IOException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                channel.sendMessage(event.getAuthor().getAsMention() + " You are using an old account, or your data was corrupted").queue();
            }

        }
        if (event.getMessage().getContentRaw().toLowerCase().startsWith("ithf")) {
            String UserFile = userProfile + ".json";
            String requestAmt = event.getMessage().getContentRaw();
            String choice = " ";
            int i = 0;
            int amtInt = 0;
            boolean space = false;
            boolean error = false;
            int accountValue = 0;
            String credits = accounts.getCredits(userProfile);
            /////////////////////////////////////////////////////////////////////////////////////////////////
            try (FileReader reader = new FileReader(UserFile)) {
                //Read JSON file
                System.out.println(credits);
                accountValue = Integer.parseInt(credits);
            } catch (FileNotFoundException e) {
                channel.sendMessage(event.getAuthor().getAsMention() + " You didnt have an account, make one with itnew").queue();
                error = true;
                e.printStackTrace();
            } catch (IOException | IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            ///////////////////////////////////////////////////////////////////////////////////////////////////
            if (requestAmt.substring(4, 5).isBlank()) {
                requestAmt = requestAmt.substring(5);
            }
            while (!space) {
                i++;
                try {
                    if (requestAmt.substring(i, i + 1).isBlank()) {
                        choice = requestAmt.substring(i + 1).toLowerCase();
                        space = true;
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    //Default to heads
                    channel.sendMessage("Nothing specified defaulting to Heads...").queue();

                    choice = "heads";
                    space = true;
                }
            }
            try {
                amtInt = Integer.parseInt(requestAmt.substring(0, i));
                System.out.println(amtInt);
                if (amtInt > 25000) {
                    error = true;
                }
            } catch (NumberFormatException e) {
                channel.sendMessage(event.getAuthor().getAsMention() + " **Eek** Improper formatting, try 'cshf (amount) (tails/heads)").queue();
                error = true;
            }
            System.out.println(choice);
            Random rand = new Random();
            double random = rand.nextDouble();
            System.out.println(random);
            if (accountValue >= amtInt && amtInt > 0 && amtInt <= 25000) {
                if (random >= 0.53 && (choice.equals("tails") || choice.equals("heads")) && !error) {
                    if (choice.equals("heads")) {
                        accountValue += amtInt;
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setColor(new Color(2743884));
                        eb.setAuthor("Deep Space Casino", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                        eb.addField("Result", "Coin: heads, You won! +" + amtInt, false);
                        eb.build();
                        channel.sendMessageEmbeds(eb.build()).queue();
                    }
                    if (choice.equals("tails")) {
                        accountValue += amtInt;
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setColor(new Color(2743884));
                        eb.setAuthor("Deep Space Casino", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                        eb.addField("Result", "Coin: tails, You won! +" + amtInt, false);
                        eb.build();
                        channel.sendMessageEmbeds(eb.build()).queue();
                    }
                } else if (choice.equals("tails") || choice.equals("heads") && !error) {
                    if (choice.equals("heads")) {
                        accountValue -= amtInt;
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setColor(new Color(0xD92222));
                        eb.setAuthor("Deep Space Casino", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                        eb.addField("Result", "Coin: tails, You lose -" + amtInt, false);
                        eb.build();
                        channel.sendMessageEmbeds(eb.build()).queue();
                    } else if (choice.equals("tails")) {
                        accountValue -= amtInt;
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setColor(new Color(0xD92222));
                        eb.setAuthor("Deep Space Casino", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                        eb.addField("Result", "Coin: heads, You lose -" + amtInt, false);
                        eb.build();
                        channel.sendMessageEmbeds(eb.build()).queue();
                    }
                } else if (!error) {
                    channel.sendMessage(event.getAuthor().getAsMention() + " **Eek** Improper formatting, try 'ithf (amount) (tails/heads)").queue();
                    error = true;
                }
            } else if (!error) {
                channel.sendMessage(event.getAuthor().getAsMention() + " Not enough credits! Do itCredits to get your credit total. ").queue();

            } else {
                channel.sendMessage(event.getAuthor().getAsMention() + " Max credits per hf is 25k!").queue();
            }
            //////////////////////////////////////////////////////////
            accounts.editAccount(userProfile, String.valueOf(accountValue));
        }
        if (event.getMessage().getContentRaw().toLowerCase().startsWith("itadd")) {
            if (admin) {
                int i = 0;
                int a = 0;
                String creditString = "";
                int creditInt = 0;
                String credits = "0";
                if (event.getMessage().getContentRaw().toLowerCase().substring(5, 5).isBlank()) {
                    creditString = event.getMessage().getContentRaw().toLowerCase().substring(5);
                } else {
                    channel.sendMessage("No space found, use the command like this, itadd (amount) (userID)").queue();
                }
                i = 5;
                a = 6;
                boolean end = false;

                while (a < event.getMessage().getContentRaw().toLowerCase().length() && !end) {
                    if (event.getMessage().getContentRaw().toLowerCase().substring(a, a + 1).isBlank()) {
                        creditString = event.getMessage().getContentRaw().toLowerCase().substring(i + 1, a);
                        System.out.println(creditString);
                        end = true;
                    }
                    a++;
                }

                String userID = event.getMessage().getContentRaw().toLowerCase().substring(a);
                String UserFile = userID + ".json";
                System.out.println(userID);
                try (FileReader reader = new FileReader(UserFile)) {
                    //Read JSON file
                    credits = accounts.getCredits(userID);
                    creditInt = Integer.parseInt(credits);

                } catch (FileNotFoundException e) {
                    channel.sendMessage(event.getAuthor().getAsMention() + "Incorrect User Id specified").queue();
                    end = true;
                    e.printStackTrace();
                } catch (IOException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                try (FileWriter newUser = new FileWriter(UserFile)) {
                    accounts.editAccount(userID, String.valueOf((creditInt + Integer.parseInt(creditString))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                channel.sendMessage(event.getAuthor().getAsMention() + " Permission Denied: I knew someone would try this ").queue();
            }

        }
        if (event.getMessage().getContentRaw().toLowerCase().startsWith("itlower")) {
            Random rand = new Random();
            double randouble = rand.nextDouble() * 100;
            int randomNum = (int) randouble;
            System.out.println(randomNum);
            String number, wager;
            double wagerDouble;
            int accountValue;
            boolean error = false;
            int numberInt = 0, wagerInt = 0;

            String userString = event.getMessage().getContentRaw().toLowerCase().substring(6);
            Pattern pattern = Pattern.compile("\\s\\d\\d\\s|\\s\\d\\s");
            Matcher matcher = pattern.matcher(userString);
            boolean matchFound = matcher.find();

            if (matchFound) {
                number = matcher.group().substring(1, matcher.group().length() - 1);
                wager = userString.substring(number.length() + 3);
                int credits = Integer.parseInt(accounts.getCredits(userProfile));
                accountValue = credits;
                int newWagerInt = 0;
                try {
                    numberInt = Integer.parseInt(number);
                } catch (NumberFormatException e) {
                    channel.sendMessage(event.getAuthor().getAsMention() + " Make sure your are using (0-99) (wager) it seems (0-99 isnt) a number?").queue();
                    error = true;

                }
                if (!error) {
                    try {
                        wagerInt = Integer.parseInt(wager);
                    } catch (NumberFormatException e) {
                        channel.sendMessage(event.getAuthor().getAsMention() + " Make sure your are using (0-99) (wager) it seems your wager isnt a number?").queue();
                        error = true;
                    }
                }
                if (wagerInt > 10000 && !error) {
                    channel.sendMessage(event.getAuthor().getAsMention() + " Max wager 10k Credits").queue();
                    error = true;
                }
                if (wagerInt <= 99 && !error) {
                    channel.sendMessage(event.getAuthor().getAsMention() + " You must wager more then 100 credits").queue();
                    error = true;
                }
                if (wagerInt <= accountValue && !error && accountValue != -1)
                {

                    if (numberInt > randomNum) {
                        if (numberInt > 50) {
                            wagerDouble = (((2 - (numberInt / 100.000)) * wagerInt) - wagerInt) * 0.91;
                        } else if (numberInt < 50) {
                            wagerDouble = (((100.0000 / numberInt) * wagerInt)) * 0.91;
                        } else if (numberInt == 50) {
                            wagerDouble = (wagerInt * 0.91);
                        } else {
                            wagerDouble = 0;
                        }
                        newWagerInt = (int) wagerDouble;
                        if (newWagerInt <= 10 && newWagerInt != 0) {
                            newWagerInt = newWagerInt - 1;
                        }
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setColor(new Color(2743884));
                        eb.setAuthor("Deep Space Casino", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                        eb.addField("Rolled:", "" + randomNum, false);
                        eb.addField("You won!", "+" + newWagerInt, false);
                        eb.build();
                        channel.sendMessageEmbeds(eb.build()).queue();
                        accountValue = accountValue + newWagerInt;
                    } else {
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setColor(new Color(0xD92222));
                        eb.setAuthor("Deep Space Casino", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                        eb.addField("Rolled:", "" + randomNum, false);
                        eb.addField("You lost", "-" + wagerInt, false);
                        eb.build();
                        channel.sendMessageEmbeds(eb.build()).queue();
                        accountValue = accountValue - wagerInt;
                    }
                } else if (wagerInt > accountValue && accountValue != -1 && !error) {
                    channel.sendMessage(event.getAuthor().getAsMention() + " You dont have enough credits").queue();
                }
                String userID = event.getAuthor().getId();
                if (accountValue != -1) {
                    accounts.editAccount(userProfile, String.valueOf(accountValue));
                }
            } else {
                channel.sendMessage(event.getAuthor().getAsMention() + " Use the command like this: itlower (Number between 0-99) then the credits you want to wager").queue();
            }
        }
        if (event.getMessage().getContentRaw().toLowerCase().startsWith("itnew")) {
            String returnVal = (accounts.newAccount(event.getAuthor().getId(), "1000"));
            if (returnVal.equals("Account already exists")) {
                channel.sendMessage(event.getAuthor().getAsMention() + " You already have an account").queue();
                System.out.println(returnVal);
            } else if (returnVal.equals("Success")) {
                channel.sendMessage(event.getAuthor().getAsMention() + " Account creation successful").queue();
                System.out.println(returnVal);
            } else {
                System.out.println(returnVal);
            }
        }
        if (event.getMessage().getContentRaw().toLowerCase().startsWith("itsend")) {
            receiverString = event.getMessage().getContentRaw().toLowerCase().substring(5);
            Pattern pattern = Pattern.compile("\\s");
            Matcher matcher = pattern.matcher(receiverString);
            boolean matchFound = matcher.find();
            boolean error = false;
            authorID = event.getAuthor().getId();
            credits = "";
            if (matchFound) {
                receiverString = receiverString.substring(matcher.group().length() + 4);
                System.out.println(receiverString);
                int i = 0;
                int accountValue = -1;
                while (i < receiverString.length()) {
                    if (receiverString.substring(i, i + 1).isBlank()) {
                        credits = receiverString.substring(i + 1);
                        try {
                            receiverString = receiverString.substring(0, i - 1);
                        } catch (IndexOutOfBoundsException e) {
                            channel.sendMessage(event.getAuthor().getAsMention() + " Improper formatting try, itsend (user) (credits)").queue();
                        }
                    }
                    i++;
                }
                System.out.println(credits);
                System.out.println(receiverString);
                try {
                    accountValue = Integer.parseInt(accounts.getCredits(userProfile));
                } catch (ClassCastException e) {
                    error = true;
                    channel.sendMessage(event.getAuthor().getAsMention() + " You do not have an account or you are using an old account").queue();
                }
                try {
                    Integer.parseInt(credits);
                } catch (NumberFormatException e) {
                    error = true;
                    channel.sendMessage("Improper formatting, it should be a number after the persons name. Ex.) itsend @deepspacecasino (credits)").queue();
                }
                if (Integer.parseInt(credits) > accountValue && !error) {
                    try {
                        int checkAcc = Integer.parseInt(accounts.getCredits(receiverString));
                    } catch (ClassCastException e) {
                        error = true;
                        channel.sendMessage(event.getAuthor().getAsMention() + " Receiving user doesnt have an account or has an old account").queue();
                    }
                    error = true;
                    channel.sendMessage(event.getAuthor().getAsMention() + " You dont have enough credits").queue();
                } else {
                    EmbedBuilder eb = new EmbedBuilder();
                    String returnVal = accounts.sendCredits(authorID, receiverString, credits);
                    if (returnVal == "") {
                        eb.setColor(new Color(0xE2BB46));
                        eb.setAuthor("Deep Space Casino", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                        eb.addField("Successfully sent: " + credits, "", false);
                        channel.sendMessageEmbeds(eb.build()).queue();
                    } else {
                        channel.sendMessage(returnVal).queue();
                    }
                }

            }
        }
        if (event.getMessage().getContentRaw().toLowerCase().startsWith("itclean")) {
            accounts.accountCleanup(userProfile);
        }
        if (event.getMessage().getContentRaw().toLowerCase().startsWith("itcrates")) {
            boolean error = false;
            boolean menu = false;
            String userString = event.getMessage().getContentRaw().toLowerCase().substring(7);

            Message msg = event.getMessage();
            Pattern pattern = Pattern.compile("\\s\\d");
            try
            {
                String temp = userString.substring(1,3);
                //System.out.println(temp);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                menu = true;
            }
            if(!menu) {
                Matcher matcher = pattern.matcher(userString.substring(1, 3));
                String crateAmount = "";
                boolean matchFound = matcher.find();

                Pattern pattern2 = Pattern.compile("\\s[i]");
                Matcher matcher2 = pattern2.matcher(userString);
                boolean matchFound2 = matcher2.find();

                Pattern pattern4 = Pattern.compile("\\s[o]");
                Matcher matcher4 = pattern4.matcher(userString);
                boolean matchFound4 = matcher4.find();
                if (matchFound) {
                    String crateNum = matcher.group().substring(1);
                    int crateID = -1;
                    int crateQuantity = 0;
                    int cost = 0;
                    int accountValue = 0;
                    System.out.println(matcher.group());
                    Pattern pattern3 = Pattern.compile("\\s\\d");
                    Matcher matcher3 = pattern3.matcher(userString.substring(2));
                    boolean matchFound3 = matcher3.find();
                    if (matchFound3) {
                        String credits = accounts.getCredits(userProfile);
                        System.out.println(credits);
                        crateAmount = matcher3.group().substring(1);
                        System.out.println(matcher3.group());
                        System.out.println("Crate number: " + crateNum + " Crate Amount:" + crateAmount);
                    } else {
                        channel.sendMessage("Please type the quantity that you would like to purchase after the crate ID. Ex.) itcrates 2 5").queue();
                        error = true;
                    }
                    try {
                        crateID = Integer.parseInt(crateNum);
                    } catch (NumberFormatException e) {
                        channel.sendMessage("Please type the crate ID that you would like to purchase then the quantity. Ex.) itcrates 3 5").queue();
                        System.out.println(crateNum + "" + crateAmount);
                        error = true;
                    }
                    if ((crateID == 1 || crateID == 2 || crateID == 3) && !error) {
                        try {
                            crateQuantity = Integer.parseInt(crateAmount);
                        } catch (NumberFormatException e) {
                            System.out.println(crateAmount);
                            channel.sendMessage("Please type the quantity that you would like to purchase after the crate ID. Ex.) itcrates 2 5").queue();
                            error = true;
                        }
                        if (crateID == 1) {
                            cost = Integer.parseInt(crateAmount) * 500;
                        } else if (crateID == 2) {
                            cost = Integer.parseInt(crateAmount) * 2000;
                        } else if (crateID == 3) {
                            cost = Integer.parseInt(crateAmount) * 5000;
                        } else if (!error) {
                            error = true;
                            channel.sendMessage("You must enter a proper crate ID. Ex.) itcrates 1 5").queue();
                        }
                        int[] crates = accounts.getCrates(userProfile);
                        if (crates[0] == -1) {
                            channel.sendMessage("File could not be found try itnew").queue();
                        } else if (crates[0] == -2) {
                            channel.sendMessage("Error: error, please contact Awesome_Wow#3043").queue();
                        } else {
                            //Valid crateID between 1-3
                            try {
                                accountValue = Integer.parseInt(accounts.getCredits(userProfile));
                            } catch (ClassCastException e) {
                                error = true;

                                channel.sendMessage("Account corrupted? please contact Awesome_Wow#3034");

                            }
                            if (crateID == 1 && !error) {
                                accountValue = Integer.parseInt(accounts.getCredits(userProfile));
                                if (accountValue >= cost) {
                                    accounts.editAccount(userProfile, Integer.toString(accountValue - cost));
                                    accounts.editCrates(userProfile, crates[0] + Integer.parseInt(crateAmount), crates[1], crates[2]);
                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setColor(new Color(0xE2BB46));
                                    eb.setAuthor("Deep Space Casino", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                                    eb.addField("Successfully bought: " + crateAmount + " tier 1 crate(s) <:asteroidt1:866166261087207464>", "", false);
                                    channel.sendMessageEmbeds(eb.build()).queue();
                                }

                            } else if (crateID == 2 && !error) {
                                accountValue = Integer.parseInt(accounts.getCredits(userProfile));
                                if (accountValue >= cost) {
                                    accounts.editAccount(userProfile, Integer.toString(accountValue - cost));
                                    accounts.editCrates(userProfile, crates[0], crates[1] + Integer.parseInt(crateAmount), crates[2]);
                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setColor(new Color(0xE2BB46));
                                    eb.setAuthor("Deep Space Casino", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                                    eb.addField("Successfully bought: " + crateAmount + " tier 2 crate(s) <:asteroidt2:866166260487815189>", "", false);
                                    channel.sendMessageEmbeds(eb.build()).queue();
                                }


                            } else if (crateID == 3 && !error) {
                                accountValue = Integer.parseInt(accounts.getCredits(userProfile));
                                if (accountValue >= cost) {
                                    accounts.editAccount(userProfile, Integer.toString(accountValue - cost));
                                    accounts.editCrates(userProfile, crates[0], crates[1], crates[2] + Integer.parseInt(crateAmount));
                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setColor(new Color(0xE2BB46));
                                    eb.setAuthor("Deep Space Casino", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                                    eb.addField("Successfully bought: " + crateAmount + " tier 3 crate(s) <:asteroidt3:866166261040545833>", "", false);
                                    channel.sendMessageEmbeds(eb.build()).queue();
                                }

                            }

                        }

                    } else if (!error) {
                        System.out.println(crateNum + " " + crateAmount);
                        channel.sendMessage("Please type the crate ID that you would like to purchase then the quantity. Ex.) itcrates 1 5").queue();
                        error = true;
                    }
                } else if (matchFound2) {
                    //Inventory Screen
                    int crates[] = accounts.getCrates(userProfile);
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setColor(new Color(0xE2BB46));
                    eb.setAuthor("Deep Space Casino", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                    eb.addField("Your Inventory:", "", true);
                    eb.addField("Tier 1 <:asteroidt1:866166261087207464>", String.valueOf(crates[0]), false);
                    eb.addField("Tier 2 <:asteroidt2:866166260487815189>", String.valueOf(crates[1]), false);
                    eb.addField("Tier 3 <:asteroidt3:866166261040545833>", String.valueOf(crates[2]), false);
                    eb.build();
                    channel.sendMessageEmbeds(eb.build()).queue();

                } else if (matchFound4) {
                    Pattern pattern5 = Pattern.compile("\\s\\d");
                    Matcher matcher5 = pattern5.matcher(userString.substring(2));
                    boolean matchFound5 = matcher5.find();
                    if (matchFound5) {
                        int crateOpening = Integer.parseInt(matcher5.group().substring(1, 2));
                        System.out.println(crateOpening);
                        int crates[] = accounts.getCrates(userProfile);
                        double rand = Math.random();
                        boolean noSuchCrate = false;
                        int crateCreds = 0;
                        if ((crateOpening == 1 && crates[0] == 0) || (crateOpening == 2 && crates[1] == 0) || (crateOpening == 3 && crates[2] == 0)) {
                            channel.sendMessage("You dont have any of that crate, purchase one with itcrates (crateNum) (amount)").queue();
                            noSuchCrate = true;
                        } else {
                            if (crateOpening == 1) {
                                if (crates[0] >= 1) {
                                    crates[0] = crates[0] - 1;
                                    accounts.editCrates(userProfile, crates[0], crates[1], crates[2]);
                                    if (accounts.getCrates(userProfile).length > 5) {
                                        accounts.accountCleanup(userProfile);
                                    }
                                    //OPENING CODE
                                    if (rand < 0.341)//1 Standard deviation above
                                    // Between 450-550
                                    {
                                        int randomCredits = new Random().nextInt(101);
                                        crateCreds = 450 + randomCredits;

                                    } else if (rand >= 0.341 && rand < 0.682)
                                    //1 Standard deviation below
                                    //between 350-450
                                    {
                                        int randomCredits = new Random().nextInt(101);
                                        crateCreds = 350 + randomCredits;

                                    } else if (rand >= 0.682 && rand < 0.818)
                                    //2 Standard deviations above
                                    //between 550-650
                                    {
                                        int randomCredits = new Random().nextInt(101);
                                        crateCreds = 450 + randomCredits;

                                    } else if (rand >= 0.818 && rand < 0.954) {
                                        //2 Standard deviations below
                                        //between 250-350
                                        int randomCredits = new Random().nextInt(101);
                                        crateCreds = 250 + randomCredits;

                                    } else if (rand >= 0.954 && rand < 0.9765)
                                    //3 Standard deviations above
                                    //between 650-750
                                    {
                                        int randomCredits = new Random().nextInt(101);
                                        crateCreds = 650 + randomCredits;

                                    } else if (rand >= 0.9765) {
                                        //3 Standard deviations below
                                        //between 150-250
                                        int randomCredits = new Random().nextInt(101);
                                        crateCreds = 150 + randomCredits;

                                    }
                                    System.out.println(rand);
                                    System.out.println(crateCreds);
                                }
                            } else if (crateOpening == 2) {
                                if (crates[1] >= 1) {
                                    crates[1] = crates[1] - 1;

                                    accounts.editCrates(userProfile, crates[0], crates[1], crates[2]);
                                    //OPENING CODE
                                    // Starting at 1800 standard deviation of 450
                                    if (rand < 0.341)
                                    //1800-2250
                                    {
                                        int randomCredits = new Random().nextInt(451);
                                        crateCreds = 1800 + randomCredits;
                                    } else if (rand >= 0.341 && rand < 0.682)
                                    //1350-1800
                                    {
                                        int randomCredits = new Random().nextInt(451);
                                        crateCreds = 1350 + randomCredits;
                                    } else if (rand >= 0.682 && rand < 0.818)
                                    //2250-2700
                                    {
                                        int randomCredits = new Random().nextInt(451);
                                        crateCreds = 2250 + randomCredits;
                                    } else if (rand >= 0.818 && rand < 0.954)
                                    //900-1350
                                    {
                                        int randomCredits = new Random().nextInt(451);
                                        crateCreds = 900 + randomCredits;
                                    } else if (rand >= 0.954 && rand < 0.9765)
                                    //2700-3150
                                    {
                                        int randomCredits = new Random().nextInt(451);
                                        crateCreds = 2700 + randomCredits;
                                    } else if (rand >= 0.9765)
                                    //450-900
                                    {
                                        int randomCredits = new Random().nextInt(451);
                                        crateCreds = 450 + randomCredits;
                                    }
                                    System.out.println(crateCreds);

                                }
                            } else if (crateOpening == 3) {
                                if (crates[2] >= 1) {
                                    crates[2] = crates[2] - 1;
                                    accounts.editCrates(userProfile, crates[0], crates[1], crates[2]);
                                    //OPENING CODE
                                    // Starting at 4500 standard deviation of 900
                                    if (rand < 0.341)
                                    //4500-5400
                                    {
                                        int randomCredits = new Random().nextInt(901);
                                        crateCreds = 4500 + randomCredits;
                                    } else if (rand >= 0.341 && rand < 0.682)
                                    //3600-4500
                                    {
                                        int randomCredits = new Random().nextInt(901);
                                        crateCreds = 3600 + randomCredits;
                                    } else if (rand >= 0.682 && rand < 0.818)
                                    //5400-6300
                                    {
                                        int randomCredits = new Random().nextInt(901);
                                        crateCreds = 5400 + randomCredits;
                                    } else if (rand >= 0.818 && rand < 0.954)
                                    //2700-3600
                                    {
                                        int randomCredits = new Random().nextInt(901);
                                        crateCreds = 2700 + randomCredits;
                                    } else if (rand >= 0.954 && rand < 0.9765)
                                    //6300-7200
                                    {
                                        int randomCredits = new Random().nextInt(901);
                                        crateCreds = 6300 + randomCredits;
                                    } else if (rand >= 0.9765)
                                    //1800-2700
                                    {
                                        int randomCredits = new Random().nextInt(901);
                                        crateCreds = 1800 + randomCredits;
                                    }
                                    System.out.println(crateCreds);
                                }
                            } else {
                                channel.sendMessage("Please type the crate ID that you would like to open. Ex.) itcrates o (crateID {1,2,3})").queue();
                                error = true;
                            }
                        }
                        if (!noSuchCrate) {
                            EmbedBuilder eb = new EmbedBuilder();
                            eb.setColor(new Color(0xE2BB46));
                            eb.setAuthor("Deep Space Casino", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                            eb.addField("Opening", ". . .", false);
                            eb.build();

                            channel.sendMessageEmbeds(eb.build()).queue();

                            List<Message> history = event.getTextChannel().getHistory().retrievePast(6).completeAfter(100, TimeUnit.MILLISECONDS);
                            Boolean edits = false;
                            for (int i = 0; i < history.size(); i++) {
                                if (history.get(i).getAuthor().getId().equals("779518180237639721") && !edits) {
                                    try {
                                        if (history.get(i).getEmbeds().get(0).getFields().get(0).getValue().equals(". . .")) {
                                            String openingMSG = null;
                                                edits = true;
                                                EmbedBuilder editedEB = new EmbedBuilder();
                                                editedEB.setColor(new Color(0xE2BB46));
                                                editedEB.setAuthor("Deep Space Casino", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                                                editedEB.addField("You won +", String.valueOf(crateCreds), false);
                                                history.get(i).editMessageEmbeds(editedEB.build()).queueAfter(75, TimeUnit.MILLISECONDS);

                                            //TODO fix this shit

                                            int _credits = Integer.parseInt(accounts.getCredits(userProfile)) + crateCreds;
                                            accounts.editAccount(userProfile, String.valueOf(_credits));


                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                    }
                                }
                            }
                        }
                    }

                }
            }
            else
            {
                //Shop Overview
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xE2BB46));
                eb.setAuthor("Deep Space Casino", "https://discord.gg/hZHQ9ge9yH", "https://cdn.discordapp.com/avatars/779518180237639721/f6a599b282b2f30dac67b4855d433002.png?size=1024");
                eb.addField("Asteroids Shop:", "To buy do itcrates (crate ID: 1,2,3) (quantity)", true);
                eb.addField("Tier 1 <:asteroidt1:866166261087207464>", "Price: 500", false);
                eb.addField("Tier 2 <:asteroidt2:866166260487815189>", "Price: 2000", false);
                eb.addField("Tier 3 <:asteroidt3:866166261040545833>", "Price: 5000", false);
                eb.build();
                channel.sendMessageEmbeds(eb.build()).queue();
            }

        }
        if (event.getMessage().getContentRaw().toLowerCase().startsWith("itprofit") && admin==true)
        {

        }
    }
}
