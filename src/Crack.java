import org.apache.commons.codec.digest.Crypt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * @author Trevor Hartman
 *
 * @author crazyne55
 *
 * 3/31/2023 @ 1657
 */
public class Crack {
    private final User[] users;
    private final String dictionary;

    public Crack(String shadowFile, String dictionary) throws FileNotFoundException {
        this.dictionary = dictionary;
        this.users = Crack.parseShadow(shadowFile);
    }

    public void crack() throws FileNotFoundException {
        int lc = getLineCount(dictionary);
        boolean[] ignoreUser = new boolean[users.length];
        FileInputStream fStream = new FileInputStream(dictionary);
        Scanner fScanner = new Scanner(fStream);
        for (int i = 0; i < lc; i++) {
            String line = fScanner.nextLine();
            for (int j = 0; j < users.length; j++) {
                if(ignoreUser[j]) continue;
                User user = users[j];
                if(!(user.getPassHash().equals("*")|user.getPassHash().equals("!")|user.getPassHash().equals("!!"))) {
                    String hash = Crypt.crypt(line, user.getPassHash());
                    if(hash.equals(user.getPassHash())) {
                        ignoreUser[j] = true;
                        System.out.printf("Found password \"%s\" for user \"%s\".",line,user.getUsername());
                        System.out.println();
                    }
                }else {
                    ignoreUser[j] = true;
                    //System.out.printf("User \"%s\" might be locked or has no password.",user.getUsername());
                    //System.out.println();
                }
            }
        }

    }

    public static int getLineCount(String path) {
        int lineCount = 0;
        try (Stream<String> stream = Files.lines(Path.of(path), StandardCharsets.UTF_8)) {
            lineCount = (int)stream.count();
        } catch(IOException ignored) {}
        return lineCount;
    }

    public static User[] parseShadow(String shadowFile) throws FileNotFoundException {
        int lc = getLineCount(shadowFile);

        User[] users1 = new User[lc];
        // I know you said to use a *while* loop, but if we do it that way, why do we need to find the line count? -crazyne55 @ 3/31/2023 1054
        // nevermind, I now understand why. Each line is a different user and we need to tell the array how many elements when we make it -crazyne55 @ 3/31/2023 1055
        FileInputStream fStream = new FileInputStream(shadowFile);
        Scanner fScanner = new Scanner(fStream);
        for (int i = 0; i < lc; i++) {
            String line = fScanner.nextLine();
            String[] info = line.split(":");
            users1[i] = new User(info[0],info[1]);
        }
        return users1;
    }

    public static void main(String[] args) throws FileNotFoundException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Type the path to your shadow file: ");
        String shadowPath = sc.nextLine();
        System.out.print("Type the path to your dictionary file: ");
        String dictPath = sc.nextLine();

        Crack c = new Crack(shadowPath, dictPath);
        c.crack();
    }
}

class User { // removed "public" so the IDE stops yelling at me -crazyne55 @ 3/31/2023 1200
    private final String username;
    private final String passHash;

    public User(String username, String passHash) {
        this.username = username;
        this.passHash = passHash;
    }

    public String getPassHash() {
        return passHash;
    }

    public String getUsername() {
        return username;
    }
    // Wow this IDE is pretty cool, all I had to type was "get" and it auto-completed the rest. -crazyne55 @ 3/31/2023 1040
}