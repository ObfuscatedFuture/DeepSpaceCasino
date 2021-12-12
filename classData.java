
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.parser.JSONParser;

import org.json.simple.parser.ParseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class classData extends ListenerAdapter{
    JSONObject obj = new JSONObject();
    JSONArray array = new JSONArray();
    JSONParser parser = new JSONParser();
    public String newAccount(String userID, String credits) {
        String FOS = "No info";
        try (FileReader reader = new FileReader(userID+".json"))
        {
            JSONObject obj = (JSONObject) parser.parse(reader);
            String fileValue = (String) obj.get("Credits:");
            FOS = "Account already exists";
        }
        catch (FileNotFoundException e)
        {
            try (FileWriter userAccount = new FileWriter(userID+".json")) {

                obj.put("ID:", userID);
                obj.put("Credits:", credits);

                array.clear();
                array.add(0);
                array.add(0);
                array.add(0);
                array.set(0, 0);
                array.set(0, 0);
                array.set(0, 0);

                obj.put("Crates:", array);
                userAccount.write(obj.toJSONString());

            } catch (IOException a) {
                a.printStackTrace();
            }
            FOS = "Success";

        }catch (IOException e)
        {
            FOS = "Failed";
            e.printStackTrace();
        } catch (ParseException e)
        {
            e.printStackTrace();
            FOS = "Faile";
        }
        return FOS;
    }
    public String getID(String userID)
    {
        String fileValue = "hmmmm";
        try (FileReader reader = new FileReader(userID+".json"))
        {
            JSONObject obj = (JSONObject) parser.parse(reader);
            fileValue = (String) obj.get("ID:");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fileValue = "File couldnt be found";

        } catch (IOException | ParseException e) {
            e.printStackTrace();
            fileValue = "Unspecified Error";
        }
        return fileValue;
    }
    public String getCredits(String userID)
    {
        String fileValue = "hmmmm";
        try (FileReader reader = new FileReader(userID+".json"))
        {
            JSONObject obj = (JSONObject) parser.parse(reader);
            fileValue = String.valueOf(obj.get("Credits:"));
        } catch (FileNotFoundException e) {
            fileValue = "File couldnt be found";

        } catch (ParseException e) {
            e.printStackTrace();
            fileValue = "Unspecified Error";
        }
        catch (ClassCastException e)
        {
            fileValue = "Old or Corrupted account";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileValue;
    }
    public int[] getCrates(String userID)
    {
        int t1 = 0;
        int t2 = 0;
        int t3 = 0;
        int[] crates = new int[]{t1,t2,t3};
        try (FileReader reader = new FileReader(userID+".json"))
        {
            JSONObject obj = (JSONObject) parser.parse(reader);
            JSONArray arr = (JSONArray) obj.get("Crates:");
            crates = new int[]{(int)(long) arr.get(0), (int) (long)arr.get(1), (int) (long)arr.get(2)};
        } catch (FileNotFoundException e) {
            crates = new int[]{-1, -1, -1};
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            crates = new int[]{-2, -1, -1};
            e.printStackTrace();
        }
        return crates;
    }
    public void editAccount(String userID, String credits)
    {
        int crates[] = getCrates(userID);
        //JSONArray array = (JSONArray) obj.get("Crates:");

        try (FileWriter userAccount = new FileWriter(userID+".json")) {
            obj.put("ID:", userID);
            obj.put("Credits:", credits);
            array.clear();
            array.add(0);
            array.add(0);
            array.add(0);
            array.set(0, crates[0]);
            array.set(1, crates[1]);
            array.set(2, crates[2]);
            obj.put("Crates:", array);
            userAccount.write(obj.toJSONString());
        } catch (IOException a) {
            a.printStackTrace();
        }
    }
    public void editCrates(String userID, int tier1, int tier2, int tier3)
    {
        String userCredits = getCredits(userID);
        try (FileWriter userAccount = new FileWriter(userID+".json"))
        {
            obj.put("ID:", userID);
            obj.put("Credits:", userCredits);
            array.clear();
            array.add(0);
            array.add(0);
            array.add(0);
            array.set(0, tier1);
            array.set(1, tier2);
            array.set(2, tier3);
            obj.put("Crates:", array);
            userAccount.write(obj.toJSONString());
        }
        catch (IOException a) {
            a.printStackTrace();
        }
    }
    public void accountCleanup(String userID)
    {
        int crates[] = getCrates(userID);
        crates = new int[]{crates[0], crates[1], crates[2]};
        editCrates(userID, crates[0], crates[1], crates[2]);
    }
    public String sendCredits(String sender, String receiver, String credits)
    {
        int senderCredits = Integer.parseInt(getCredits(sender));
        String returnVal = "";
        int receiverCredits = 0;
        if(senderCredits>=Integer.parseInt(credits) && Integer.parseInt(credits)>=0) {
            try
            {
                receiverCredits = Integer.parseInt(getCredits(receiver));
            }
            catch(NumberFormatException e)
            {
                returnVal= "Receiver does not have an account";
            }
            try (FileWriter senderAccount = new FileWriter(sender + ".json")) {
                obj.put("ID:", sender);
                obj.put("Credits:", String.valueOf(senderCredits - (Integer.parseInt(credits))));
                int crates[] = getCrates(sender);
                array.clear();
                array.add(0);
                array.add(0);
                array.add(0);
                array.set(0, crates[0]);
                array.set(1, crates[1]);
                array.set(2, crates[2]);
                obj.put("Crates:", array);
                senderAccount.write(obj.toJSONString());
                try (FileWriter receiverAccount = new FileWriter(receiver + ".json")) {

                    obj.put("ID:", receiver);
                    obj.put("Credits:", String.valueOf(receiverCredits+(Integer.parseInt(credits))));
                    crates = getCrates(receiver);
                    array.clear();
                    array.add(0);
                    array.add(0);
                    array.add(0);
                    array.set(0, crates[0]);
                    array.set(1, crates[1]);
                    array.set(2, crates[2]);
                    obj.put("Crates:", array);
                    receiverAccount.write(obj.toJSONString());
                } catch (FileNotFoundException a)
                {
                    returnVal="Receiving user does not have an account use !new to make one";
                }
                catch (IOException a) {
                    a.printStackTrace();
                    returnVal="Other error contact Awesome_wow#3034";
                }
            }catch (FileNotFoundException a)
            {
                returnVal="Sending user does not have an account use !new to make one";
            }
            catch (IOException a) {
                a.printStackTrace();
                returnVal="Other error contact Awesome_wow#3034";
            }

        }
        else if(Integer.parseInt(credits)<0)
        {
            returnVal="You cant send less then 0 credits";
        }
        else
        {
            //Not enough credits
            returnVal="Sender doesn't have enough credits";
        }
        return returnVal;
    }
}
