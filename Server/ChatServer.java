import java.net.*;    //Internetzugriff
import java.io.*;     //Internet i/o
import java.util.*;   //HashSets
import java.awt.* ;
import java.awt.image.BufferedImage;


public class ChatServer{

        private int port;
        private Set<User> database = new HashSet<>();   //speichert User und deren Passwort
        private Set<UserThread> userThreads = new HashSet<>();  //darin werden die Threads verwaltet

        public ChatServer(int port) {
            this.port = port;
        }

        public static void main(String[] args) { //Port als String
            if (args.length < 1) {
                System.out.println("start application like this: java ChatServer <port-number>");
                System.exit(0);
            }
            
            int port = Integer.parseInt(args[0]); //wandelt String zu Integer um (Port)
            ChatServer server = new ChatServer(port);
            server.execute();
        }

//======================================================================================================

        public void execute() {
            try (ServerSocket serverSocket = new ServerSocket(this.port)) {
                System.out.println("Chat Server is listening on port " + this.port);
                Ascii("running", 12); //von Stackoverflow genommen

                while (true) {
                    Socket socket = serverSocket.accept();                                             //Verbindungsanfrage akzeptieren 
                    System.out.println("someone connected");
                    UserThread newUser = new UserThread(socket, this);                      //erstelle eigenen Thread für den User auf dem Server und starte ihn
                    userThreads.add(newUser);
                    newUser.start();
                }

            } catch (IOException e) {
                System.out.println("Error in the server: " + e.getMessage());
                e.printStackTrace();
            }
        }

        ////////////////////////////////////// MESSAGING  //////////////////////////////////////////7
        void transmit(String message, UserThread excludeUser) {         //Message schreiben (bekommen alle, nur Sender nicht)
            for (UserThread aUser : userThreads) {
                if (aUser != excludeUser && aUser.authenticated()) {
                    aUser.sendMessage(message); 
                }
            }
        }

        void transmitSingle(String message, UserThread user) {         //Nachricht an bestimmten User
             user.sendMessage(message);
        }


        ////////////////////// USER MANAGEMENT //////////////////////////////////////7

        public User getUserFromDatabase(String username){
            for (User aUser : this.database){
                if (aUser.getUsername().equals(username)){
                    return aUser;
                }
            }
            return null;
        }

        public void addUser(String username, String password){
            this.database.add(new User(username, password, true));  //true setzt User auf Online --> er wird unter "connected Users" angezeigt
        }


        public void removeUser(String userName, UserThread aUser) {         //quit chat - removes username and userthread
            this.setStatus(userName, false);                                //update status to offline
            userThreads.remove(aUser);
            System.out.println("The user " + userName + " disconnected");
        }

    
        public void changePassword(String username,String oldpassword, String newpassword){ //currently unused
            if (this.checkPassword(username,oldpassword)){
                getUserFromDatabase(username).setPassword(newpassword);
            }
        }
    
        public boolean userExists(String username){
            if (getUserFromDatabase(username) != null)
                return true;
            return false;
        }

        public void setStatus(String username, boolean onlineStatus){
            getUserFromDatabase(username).setOnline(onlineStatus);
        }


        public boolean checkOnline(String username){
            if (getUserFromDatabase(username).isOnline() == true)
                return true;
            return false;
        }
    
        public boolean checkPassword(String username, String password){
            User aUser = getUserFromDatabase(username);
                if (aUser.getUsername().equals(username) && aUser.getPassword().equals(password)){
                    return true;
                }
            
            return false;
        }

        public ArrayList<User> getOnlineUsers(){ 
            ArrayList<User> users = new ArrayList<User>();
            for (User aUser : database){
                if (aUser.isOnline() == true){
                    users.add(aUser);
                }
            }
            return users; 
        }


        //von Stackoverflow genommen
        public void Ascii(String text, int font){

            int width = 200;
            int height = 30;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            g.setFont(new Font("SansSerif", Font.BOLD, font));
    
            Graphics2D graphics = (Graphics2D) g;
            graphics.drawString(text, 10, 20);
    
            for (int y = 0; y < height; y++) {
                StringBuilder sb = new StringBuilder();
                for (int x = 0; x < width; x++) {
                    sb.append(image.getRGB(x, y) == -16777216 ? " " : "$");
                }
                if (sb.toString().trim().isEmpty()) {
                    continue;
                }
    
                System.out.println(sb);
            }
        }
}