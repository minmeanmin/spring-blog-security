package shop.mtcoding.blog._core;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

public class BCryptTest {

    @Test
    public void gensalt_test(){
        String salt = BCrypt.gensalt();
        System.out.println(salt);
    }

    // $2a$10$.3UbzZz18mVH4f98D.ygnuCM/Mv5q6AqoOYMRZeXRack6HSBBLOW6
    // $2a$10$6W6/gSirP4O3aW/0Z62/GuyQ0vTOfqKIMj6Vgs/m5SyMfQhTS1RD.
    @Test
    public void hashpw_test(){
        String rawPassword = "1234";
        String encPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        System.out.println(encPassword);
    }

}
