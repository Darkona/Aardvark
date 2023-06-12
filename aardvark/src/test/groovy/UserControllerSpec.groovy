import com.darkona.aardvark.controller.ErrorHandlingControllerAdvice
import com.darkona.aardvark.controller.UserController
import com.darkona.aardvark.domain.Login
import com.darkona.aardvark.domain.User
import com.darkona.aardvark.service.UserService
import com.darkona.aardvark.util.MapperUtil
import lombok.extern.slf4j.Slf4j
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import javax.naming.AuthenticationException

//@SpringBootTest(classes = UserController.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(loader = SpringBootContextLoader, classes = UserController.class)
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@Slf4j
class UserControllerSpec extends Specification {

    @Autowired
    UserController userController

    @SpringBean
    UserService userService = Mock()

    private MockMvc mvc

    def setup() {
        mvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new ErrorHandlingControllerAdvice())
                .build()
    }
    def mapper = new MapperUtil()

    def "Controller loads for testing"() {
        when: "a get request is sent to the /check endpoint"
        def response = mvc.perform(MockMvcRequestBuilders.get("/check"))
                .andReturn().response
        then: "should return 'ok' and HttpStatus OK"
        assert response.getContentAsString() == "ok"
        assert response.status == HttpStatus.OK.value()
    }

    def "user creation in sign-up"() {
        given: "a correctly formed user json input"

        def correct_user = mapper.readModelJson("correct_user.json")
        def complete_user = mapper.deserializeUser(mapper.readModelJson("complete_user.json"))
        userService.signUserUp((User) _) >> complete_user

        when: "a post request is sent to /sign-up with a contract-appropiate user json"
        def response = doCall(correct_user)

        then: "the response should be a properly saved new user"
        User nUser = mapper.deserializeResponseUser(response.getContentAsString())
        assert nUser.getIsActive()
        assert nUser.getId()
        assert response.status == HttpStatus.CREATED.value()
    }

    def "too few uppercase validation error in sign-up"() {
        given: "a user json input with a non-compliant password"

        def bad_password = mapper.readModelJson("bad_password_user.json")
                .replace("xxxxxxxxxxxx", "aardvark55")

        when: "a post request is sent to /sign-up with the bad password user json"
        def response = doCall(bad_password)

        then: "an error response should be sent"
        response.status == HttpStatus.BAD_REQUEST.value()
        def errorResponse = mapper.deserializeErrorResponse(response.getContentAsString())
        !errorResponse.error.isEmpty()
        errorResponse.error.stream()
                .anyMatch(e -> e.detail.contains("{validation.password.upper}"))
                .booleanValue()
        errorResponse.error.get(0).getCodigo() == HttpStatus.BAD_REQUEST.value()

    }

    def "too many uppercase password validation error in sign-up"() {
        given: "a user json input with a non-compliant password"

        def bad_password = mapper.readModelJson("bad_password_user.json")
                .replace("xxxxxxxxxxxx", "AArdvark55")

        when: "a post request is sent to /sign-up with the bad password user json"
        def response = doCall(bad_password)

        then: "an error response should be sent"
        response.status == HttpStatus.BAD_REQUEST.value()
        def errorResponse = mapper.deserializeErrorResponse(response.getContentAsString())
        !errorResponse.error.isEmpty()
        errorResponse.error.get(0).getCodigo() == HttpStatus.BAD_REQUEST.value()
        errorResponse.error.stream()
                .anyMatch(e -> e.detail.contains("validation.password.upper"))
                .booleanValue()
    }

    def "too long password validation error in sign-up"() {
        given: "a user json input with a non-compliant password"

        def bad_password = mapper.readModelJson("bad_password_user.json")
                .replace("xxxxxxxxxxxx", "Aardvark55ants")

        when: "a post request is sent to /sign-up with the bad password user json"
        def response = doCall(bad_password)

        then: "an error response should be sent"
        response.status == HttpStatus.BAD_REQUEST.value()
        def errorResponse = mapper.deserializeErrorResponse(response.getContentAsString())
        !errorResponse.error.isEmpty()
        errorResponse.error.get(0).getCodigo() == HttpStatus.BAD_REQUEST.value()
        errorResponse.error.get(0).getDetail().contains("validation.password.length")
    }

    def "too short password validation error in sign-up"() {
        given: "a user json input with a non-compliant password"

        def bad_password = mapper.readModelJson("bad_password_user.json")
                .replace("xxxxxxxxxxxx", "Arrdvar")

        when: "a post request is sent to /sign-up with the bad password user json"
        def response = doCall(bad_password)

        then: "an error response should be sent"
        response.status == HttpStatus.BAD_REQUEST.value()
        def errorResponse = mapper.deserializeErrorResponse(response.getContentAsString())
        !errorResponse.error.isEmpty()
        errorResponse.error.get(0).getCodigo() == HttpStatus.BAD_REQUEST.value()
        errorResponse.error.stream()
                .anyMatch(e -> e.detail.contains("validation.password.length"))
                .booleanValue()

    }

    def "too many numbers password validation error in sign-up"() {
        given: "a user json input with a non-compliant password"

        def bad_password = mapper.readModelJson("bad_password_user.json")
                .replace("xxxxxxxxxxxx", "Aardv44rk55")

        when: "a post request is sent to /sign-up with the bad password user json"
        def response = doCall(bad_password)

        then: "an error response should be sent"
        response.status == HttpStatus.BAD_REQUEST.value()
        def errorResponse = mapper.deserializeErrorResponse(response.getContentAsString())
        !errorResponse.error.isEmpty()
        errorResponse.error.get(0).getCodigo() == HttpStatus.BAD_REQUEST.value()
        errorResponse.error.stream()
                .anyMatch(e -> e.detail.contains("validation.password.number"))
                .booleanValue()
    }

    def "too few numbers password validation error in sign-up"() {
        given: "a user json input with a non-compliant password"

        def bad_password = mapper.readModelJson("bad_password_user.json")
                .replace("xxxxxxxxxxxx", "Aardvark5")

        when: "a post request is sent to /sign-up with the bad password user json"
        def response = doCall(bad_password)

        then: "an error response should be sent"
        response.status == HttpStatus.BAD_REQUEST.value()
        def errorResponse = mapper.deserializeErrorResponse(response.getContentAsString())
        !errorResponse.error.isEmpty()
        errorResponse.error.get(0).getCodigo() == HttpStatus.BAD_REQUEST.value()
        errorResponse.error.get(0).getDetail().contains("validation.password.number")
    }

    def "empty password validation error in sign-up"() {
        given: "a user json input with a non-compliant password"

        def bad_password = mapper.readModelJson("bad_password_user.json")
                .replace("xxxxxxxxxxxx", "")

        when: "a post request is sent to /sign-up with the bad password user json"
        def response = doCall(bad_password)

        then: "an error response should be sent"
        response.status == HttpStatus.BAD_REQUEST.value()
        def errorResponse = mapper.deserializeErrorResponse(response.getContentAsString())
        !errorResponse.error.isEmpty()
        errorResponse.error.get(0).getCodigo() == HttpStatus.BAD_REQUEST.value()
        errorResponse.error.stream()
                .anyMatch(e -> e.detail.contains("validation.password.empty"))
                .booleanValue()
    }

    def "bad email validation error in sign-up"() {
        given: "a user json input with a non-compliant email"

        def bad_email = mapper.readModelJson("bad_email_user.json")

        when: "a post request is sent to /sign-up with the bad email user json"
        def response = doCall(bad_email)

        then: "an error response should be sent"
        response.status == HttpStatus.BAD_REQUEST.value()
        def errorResponse = mapper.deserializeErrorResponse(response.getContentAsString())
        !errorResponse.error.isEmpty()
        errorResponse.error.get(0).getCodigo() == HttpStatus.BAD_REQUEST.value()
        errorResponse.error.get(0).getDetail().contains("validation.email")
    }

    def "attempt duplicate sign-up"() {

        given: "a user json input with correct info but email already present in database"

            def correct_user = mapper.readModelJson("correct_user.json")
            userService.signUserUp(_ as User) >> {
                throw new DataIntegrityViolationException("msg", new Exception("msg", new Exception("Unique")))
            }

        when: "a post request is sent to /sign-up with the bad email user json"

            def response =
                    mvc.perform(MockMvcRequestBuilders
                    .post("/sign-up")
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(correct_user)
            ).andReturn().response

        then: "an error response should be sent"


        response.status == HttpStatus.CONFLICT.value()
        def errorResponse = mapper.deserializeErrorResponse(response.getContentAsString())
        !errorResponse.error.isEmpty()
        errorResponse.error.get(0).getCodigo() == HttpStatus.CONFLICT.value()
        errorResponse.error.get(0).getDetail().contains("Account already exists")
    }

    def "login attempt with correct info"(){
        given: "a login json input correct info"

            def login = mapper.readModelJson("login_credentials.json")
            def complete_user = mapper.deserializeUser(mapper.readModelJson("complete_user.json"))
            userService.getUserByLoginCredentials(_ as Login, _ as Map<String, String>) >> complete_user
            def headers = new HttpHeaders()
            headers.put("bearer", ["some token"])

        when: "a post request is sent to /sign-up with already existing user"

            def response =
                    mvc.perform(MockMvcRequestBuilders
                            .post("/login")
                            .headers(headers)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(login)
                    ).andReturn().response

        then: "an error response should be sent"
            response.status == HttpStatus.OK.value()
    }

    def "login attempt with incorrect token"(){
        given: "a login input with incorrect token"

        def login = mapper.readModelJson("login_credentials.json")
        userService.getUserByLoginCredentials(_ as Login, _ as Map<String, String>) >> {
            throw new AuthenticationException("Token")
        }
        def headers = new HttpHeaders()
        headers.put("bearer", ["some token"])

        when: "a post request is sent to /login with valid credentials"

        def response =
                mvc.perform(MockMvcRequestBuilders
                        .post("/login")
                        .headers(headers)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(login)
                ).andReturn().response

        then: "Error response for authentication"
            response
            response.status == HttpStatus.UNAUTHORIZED.value()
            def errorResponse = mapper.deserializeErrorResponse(response.getContentAsString())
            !errorResponse.error.isEmpty()
            errorResponse.error.get(0).getCodigo() == HttpStatus.UNAUTHORIZED.value()
            errorResponse.error.get(0).getDetail().contains("Token")

    }

    def "login attempt with incorrect password"(){
        given: "a login input with incorrect password"

        def login = mapper.readModelJson("login_credentials.json")
        userService.getUserByLoginCredentials(_ as Login, _ as Map<String, String>) >> {
            throw new AuthenticationException("Password")
        }
        def headers = new HttpHeaders()
        headers.put("bearer", ["some token"])

        when: "a post request is sent to /login with valid credentials"

        def response =
                mvc.perform(MockMvcRequestBuilders
                        .post("/login")
                        .headers(headers)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(login)
                ).andReturn().response

        then: "Error response for authentication"
        response
        response.status == HttpStatus.UNAUTHORIZED.value()
        def errorResponse = mapper.deserializeErrorResponse(response.getContentAsString())
        !errorResponse.error.isEmpty()
        errorResponse.error.get(0).getCodigo() == HttpStatus.UNAUTHORIZED.value()
        errorResponse.error.get(0).getDetail().contains("Password")

    }

    private MockHttpServletResponse doCall(String userRequest) {
        return mvc.perform(MockMvcRequestBuilders
                .post("/sign-up")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(userRequest)
        ).andReturn().response
    }

    @TestConfiguration
    static class MockConfig {
        def detachedMockFactory = new DetachedMockFactory()

        @Bean
        UserController userController() {
            return detachedMockFactory.Stub(UserController)
        }
    }
}
