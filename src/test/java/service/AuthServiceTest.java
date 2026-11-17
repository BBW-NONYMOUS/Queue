package service;
import dao.support.ServiceFixture;import model.*;import java.util.*;import org.junit.jupiter.api.*;import static org.junit.jupiter.api.Assertions.*;
class AuthServiceTest {
    private ServiceFixture store;private AuthService auth;
    @BeforeEach void setup(){store=new ServiceFixture();auth=new AuthService(store,12);}
    @Test void erasesPasswordsOnSuccessAndFailure()throws Exception{char[] success=ServiceFixture.PASSWORD.toCharArray();auth.login("admin",success);assertErased(success);char[] failure="wrong".toCharArray();assertThrows(AuthenticationException.class,()->auth.login("admin",failure));assertErased(failure);}
    @Test void ignoresForgedDisplayRole()throws Exception{Session student=store.login(auth,Role.STUDENT);User admin=store.accounts.get(1L);Session forged=new Session(student.token(),admin);assertEquals(Role.STUDENT,auth.currentSession(forged).user().role());assertThrows(AuthorizationException.class,()->store.read(c->auth.require(c,forged,Role.ADMIN)));}
    @Test void logoutRevokesToken()throws Exception{var session=store.login(auth,Role.ADMIN);auth.logout(session);assertThrows(AuthorizationException.class,()->auth.currentSession(session));}
    @Test void databaseRoleChangeRevokesOldSession()throws Exception{var session=store.login(auth,Role.ADMIN);store.setUserState(1,Role.OFFICER,null,1L,true,false);assertThrows(AuthorizationException.class,()->auth.currentSession(session));}
    @Test void inactiveLinkedStudentCannotAuthenticate(){var old=store.students.get(100L);store.students.put(100L,new Student(old.id(),old.studentNumber(),old.firstName(),null,old.lastName(),null,old.program(),1,old.section(),false,old.createdAt(),old.updatedAt()));assertThrows(AuthenticationException.class,()->store.login(auth,Role.STUDENT));}
    @Test void passwordChangeUsesBcryptAndRevokesOtherSessions()throws Exception{var first=store.login(auth,Role.ADMIN);var other=store.login(auth,Role.ADMIN);char[] old=ServiceFixture.PASSWORD.toCharArray(),next="Replacement-password-42".toCharArray();auth.changePassword(first,old,next);assertErased(old);assertErased(next);assertTrue(org.mindrot.jbcrypt.BCrypt.checkpw("Replacement-password-42",store.hashes.get(1L)));assertThrows(AuthorizationException.class,()->auth.currentSession(other));}
    @Test void rejectsOversizeUtf8PasswordsAndErasesArrays(){char[] password="ñ".repeat(37).toCharArray();assertThrows(AuthenticationException.class,()->auth.login("admin",password));assertErased(password);}
    private static void assertErased(char[] value){for(char c:value)assertEquals(0,c);}
}
