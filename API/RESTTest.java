package API;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;

@TestMethodOrder(MethodOrderer.MethodName.class)

public class RESTTest {


    private final static String url = "http://localhost:5000/api/books";

    @BeforeAll
    public static void setupURI() {
        RestAssured.baseURI = url;
    }

    @Test
    public void A01GETAllBooksInfo(){


            List<Book> books = given()
                    .when().contentType(ContentType.JSON).get(url).then()
                    .assertThat().statusCode(HttpStatus.SC_OK).log()
                    .all().extract().body().jsonPath().getList("books", Book.class);

            assertThat(books.size()).isGreaterThan(0);
    }
    @Disabled
    @Test
    public void A02GETAllBooksEmptyLibrary(){
        List<Book> booksToDelete = given()
                .when().contentType(ContentType.JSON).get(url).then()
                .extract().body().jsonPath().getList("books", Book.class);

        for (Book book : booksToDelete) {
            setupURI();
            RequestSpecification request = RestAssured.given();
            request.header("Content-Type", "application/json");
            request.delete(String.valueOf(book.getId()));
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "The Stand");
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        Response response = request.get(url);
        int statusCode = response.getStatusCode();
        assertThat(statusCode).isEqualTo(HttpStatus.SC_OK);
        response.prettyPeek();

    }
    @Test
    public void A03GETExistingBookInfo(){
        int existingBook = 1;
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        Response response = request.get(url+"/"+existingBook);
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        System.out.println(statusDescr);
        System.out.println(response.asPrettyString());


        assertThat(statusCode).isEqualTo(HttpStatus.SC_OK);
        assertThat(response.body().asPrettyString())
                .contains("\"name\": \"Чистый код\"").contains("\"author\": \"Роберт Мартин\"")
                .contains("\"year\": 1998").contains("\"isElectronicBook\": false");
    }

    @Test
    public void A04GETExistingBookInfoAddZeroBeforeId(){

        int existingBook = 01;
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        Response response = request.get(url+"/"+existingBook);
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        System.out.println(statusDescr);

        assertThat(statusCode).isEqualTo(HttpStatus.SC_OK);
        assertThat(given().when().get(url+"/"+existingBook).body().prettyPrint())
                .contains("\"name\": \"Чистый код\"").contains("\"author\": \"Роберт Мартин\"")
                .contains("\"year\": 1998").contains("\"isElectronicBook\": false");
    }

    @Test
    public void A05GETMissingBookInfo(){
        int id = 100;
        RequestSpecification request = RestAssured.given();
        Response response = request.get(url+"/"+100);
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        String jsonString = response.asString();
        System.out.println(statusDescr);
        System.out.println(jsonString);

        assertThat(statusCode).isEqualTo(HttpStatus.SC_NOT_FOUND);
        Assertions.assertTrue(jsonString.contains("\"error\":\"Book with id "+id+" not found\""));
    }

    @Test
    public void A06POSTBookOnlyName(){
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "The Stand");
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());
        Response response = request.post(url);
        int statusCode = response.getStatusCode();
        System.out.println(response.getStatusLine());

        Assertions.assertEquals(statusCode, HttpStatus.SC_CREATED);
        Assertions.assertTrue(response.body().prettyPrint().contains("\"name\": \"The Stand\""));
    }

    @Test
    public void A07POSTBookNameAuthorYearTrue(){
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Будущее");
        requestBody.put("author", "Дмитрий Глуховский");
        requestBody.put("year", 2013);
        requestBody.put("isElectronicBook", true);
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());
        Response response = request.post(url);
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        System.out.println(statusDescr);
        response.getBody().prettyPrint();

        Assertions.assertEquals(statusCode, 201);
        Assertions.assertTrue(response.asPrettyString().contains("\"name\": \"Будущее\""));
        Assertions.assertTrue(response.asPrettyString().contains("\"author\": \"Дмитрий Глуховский\""));
        Assertions.assertTrue(response.asPrettyString().contains("\"year\": 2013"));
        Assertions.assertTrue(response.asPrettyString().contains("\"isElectronicBook\": true"));
    }

    @Test
    public void A08POSTBookNameIdDoesNotExist(){
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Будущее");
        requestBody.put("id", 777);
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());
        Response response = request.post(url);
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        System.out.println(statusDescr);
        response.getBody().prettyPrint();

        assertThat(statusCode).isEqualTo(HttpStatus.SC_CREATED);
        Assertions.assertFalse(response.asPrettyString().contains("777"));
        Assertions.assertTrue(response.asPrettyString().contains("\"name\": \"Будущее\""));
    }

    @Test
    public void A09POSTExistingBookNameId(){
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Будущее");
        requestBody.put("id", 2);
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());
        Response response = request.post(url);
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        System.out.println(statusDescr);
        response.getBody().prettyPrint();

        assertThat(statusCode).isEqualTo(HttpStatus.SC_CREATED);
        Assertions.assertFalse(response.getBody().asPrettyString().contains("\"id\": 2"));
        Assertions.assertTrue(response.getBody().asPrettyString().contains("\"name\": \"Будущее\""));
        Assertions.assertTrue(response.getBody().asPrettyString().contains("\"year\": 0"));
        Assertions.assertTrue(response.getBody().asPrettyString().contains("\"isElectronicBook\": false"));
        Assertions.assertTrue(response.getBody().asPrettyString().contains("\"author\": \"\""));



    }

    @Test
    public void A10POSTExistingBook() {

        String putName = "Чистый код";
        String putAuthor = "Роберт Мартин";
        int putYear = 1998;
        Boolean putIsElectronicBook = false;

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", putName);
        requestBody.put("author", putAuthor);
        requestBody.put("year", putYear);
        requestBody.put("isElectronicBook", putIsElectronicBook);
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());
        Response response = request.post(url);
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        System.out.println(statusDescr);
        response.getBody().prettyPrint();

        Assertions.assertEquals(statusCode, HttpStatus.SC_CREATED);
        Assertions.assertTrue(response.getBody().asPrettyString().contains("\"name\": \"Чистый код\""));
        Assertions.assertTrue(response.getBody().asPrettyString().contains("\"year\": 1998"));
        Assertions.assertTrue(response.getBody().asPrettyString().contains("\"author\": \"Роберт Мартин\""));
        Assertions.assertTrue(response.getBody().asPrettyString().contains("\"isElectronicBook\": false"));
    }

    @Test
    public void A11POSTBookOnlyNameAsInteger() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", 5555);
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());
        Response response = request.post(url);
        System.out.println(response.statusLine());
        int statusCode = response.getStatusCode();
        String statusDescr = response.body().prettyPrint();

        assertThat(statusCode).isEqualTo(HttpStatus.SC_BAD_REQUEST);
        Assertions.assertTrue(statusDescr.contains("\"error\": \"Name must be String type (Unicode)\""));
        Assertions.assertFalse(response.asPrettyString().contains("5555"));
    }

    @Test
    public void A12POSTBookNameAuthorAsInteger() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Clockwork orange");
        requestBody.put("author", 9999);
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());
        Response response = request.post(url);
        System.out.println(response.statusLine());
        int statusCode = response.getStatusCode();
        response.body().prettyPrint();

        assertThat(statusCode).isEqualTo(HttpStatus.SC_BAD_REQUEST);
        Assertions.assertFalse(response.asPrettyString().contains("\"author\": 9999"));
        Assertions.assertFalse(response.asPrettyString().contains("\"name\": \"Clockwork orange\""));
    }
    @Test
    public void A13POSTBookNameAuthorYearAsString() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "For whom the bell tolls");
        requestBody.put("year", "TEST");
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());
        Response response = request.post(url);
        int statusCode = response.getStatusCode();
        System.out.println(response.statusLine());
        response.body().prettyPrint();

        assertThat(statusCode).isEqualTo(HttpStatus.SC_BAD_REQUEST);
        Assertions.assertFalse(response.asPrettyString().contains("\"year\": \"TEST\""));
        Assertions.assertFalse(response.asPrettyString().contains("\"name\": \"For whom the bell tolls\""));
    }

    @Test
    public void A14POSTBookNameIsElectronicAsInt() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "The Art of War");
        requestBody.put("isElectronicBook", 777);
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());
        Response response = request.post(url);
        int statusCode = response.getStatusCode();
        System.out.println(response.statusLine());
        response.body().prettyPrint();

        assertThat(statusCode).isEqualTo(HttpStatus.SC_BAD_REQUEST);
        Assertions.assertFalse(response.asPrettyString().contains("\"isElectronicBook\": 777"));
        Assertions.assertFalse(response.asPrettyString().contains("\"name\": \"The Art of War\""));
    }

    @Disabled
    @Test
    public void A15POSTBookEmptyLibrary(){
        List<Book> booksToDelete = given()
                .when()
                .contentType(ContentType.JSON)
                .get(url)
                .then()
                .extract().body().jsonPath().getList("books", Book.class);

        for (Book book : booksToDelete) {
            setupURI();
            RequestSpecification request = RestAssured.given();
            request.header("Content-Type", "application/json");
            Response response = request.delete(String.valueOf(book.getId()));
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Pet cemetery");
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.body(requestBody.toString());
        Response response2 = request.post(url);
        int statusCode = response2.getStatusCode();
        assertThat(statusCode).isEqualTo(HttpStatus.SC_CREATED);
    }

    @Test
     public void A16DELETEExistingBook(){
        String id  = "5";
        setupURI();
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        Response response = request.delete(id);
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        String jsonString = response.asString();
        System.out.println(statusDescr);
        System.out.println(jsonString);

        Assertions.assertTrue(jsonString.contains("\"result\":true"));
        assertThat(statusCode).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void A17DELETEMissingBook(){
        int id  = 999;
        setupURI();
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        Response response = request.delete(String.valueOf(id));
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        String jsonString = response.asString();
        System.out.println(statusDescr);
        System.out.println(jsonString);

        Assertions.assertEquals(statusCode, HttpStatus.SC_NOT_FOUND);
        Assertions.assertTrue(jsonString.contains("\"error\":\"Book with id "+id+" not found\""));
    }

    @Test
    public void A18DELETEBookNoId(){
        String id  = "";
        setupURI();
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        Response response = request.delete(id);
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        String jsonString = response.asString();
        System.out.println(statusDescr);
        System.out.println(jsonString);

        assertThat(statusCode).isEqualTo(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }
    @Test
    public void A19DELETEExistingBookAddZeroBeforeId(){
        int id  = 03;
        setupURI();
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        Response response = request.delete(String.valueOf(id));
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        String jsonString = response.asString();
        System.out.println(statusDescr);
        System.out.println(jsonString);

        assertThat(statusCode).isEqualTo(HttpStatus.SC_OK);
        Assertions.assertTrue(jsonString.contains("\"result\":true"));
    }

    @Test
    public void A20PUTExistingBookInfo() {

        int bookId = 2;
        String putName = "Тестиирование.COM";
        String putAuthor = "Савин";
        int putYear = 4444;
        Boolean putIsElectronicBook = false;

        JSONObject request = new JSONObject();
        request.put("name", putName);
        request.put("author", putAuthor);
        request.put("year", putYear);
        request.put("isElectronicBook", putIsElectronicBook);

        given().header("Content-Type", "application/json")
        .contentType(ContentType.JSON).accept(ContentType.JSON).body(request.toString()).when()
        .put(url+"/"+bookId).then().log().status()
                         .assertThat().statusCode(HttpStatus.SC_OK);

        assertThat(given().when().get(url+"/"+bookId).body().prettyPrint())
                .contains(putName)
                .contains(String.valueOf(putIsElectronicBook))
                .contains(putAuthor)
                .contains(String.valueOf(putYear));
    }

    @Test
    public void A21PUTeExistingBookInfoAddZeroBeforeId() {

        int bookId = 02;
        String putName = "Война и мир";
        String putAuthor = "Толстой";
        int putYear = 4444;
        Boolean putIsElectronicBook = true;

        JSONObject request = new JSONObject();
        request.put("name", putName);
        request.put("author", putAuthor);
        request.put("year", putYear);
        request.put("isElectronicBook", putIsElectronicBook);

        given().header("Content-Type", "application/json").contentType(ContentType.JSON)
                .accept(ContentType.JSON).body(request.toString()).when().put(url+"/"+bookId)
                .then().log().status()
                .assertThat().statusCode(HttpStatus.SC_OK);

        assertThat(given().when().get(url+"/"+bookId).body().prettyPrint())
                .contains(putName)
                .contains(putAuthor)
                .contains(String.valueOf(putYear))
                .contains(putIsElectronicBook.toString());
    }

    @Test
    public void A22PUTExistingBookInfoNameAsInteger() {

        int bookId = 2;
        int putName = 3333;
        String putAuthor = "RRRR";
        int putYear = 4444;
        Boolean putIsElectronicBook = true;

        JSONObject request = new JSONObject();
        request.put("name", putName);
        request.put("author", putAuthor);
        request.put("year", putYear);
        request.put("isElectronicBook", putIsElectronicBook);

        String jsonString = given().header("Content-Type", "application/json").contentType(ContentType.JSON)
                .accept(ContentType.JSON).body(request.toString()).when().put(url+"/"+bookId)
                .then().log().all()
                .assertThat().statusCode(HttpStatus.SC_BAD_REQUEST).extract().asPrettyString();

        Assertions.assertFalse(jsonString.contains(putAuthor));
        Assertions.assertFalse(jsonString.contains(String.valueOf(putName)));
        Assertions.assertFalse(jsonString.contains(String.valueOf(putYear)));
        Assertions.assertFalse(jsonString.contains(String.valueOf(putIsElectronicBook)));
        Assertions.assertTrue(jsonString.contains("\"error\": \"Name must be String type (Unicode)\""));
    }

    @Test
    public void A23PUTExistingBookInfoIsElectronicBookAsString() {

        int bookId = 2;
        String putName = "Три сестры";
        String putAuthor = "Чехов";
        int putYear = 4545;
        String putIsElectronicBook = "notTrue";

        JSONObject request = new JSONObject();
        request.put("name", putName);
        request.put("author", putAuthor);
        request.put("year", putYear);
        request.put("isElectronicBook", putIsElectronicBook);

        String jsonString = given().header("Content-Type", "application/json").contentType(ContentType.JSON)
                .accept(ContentType.JSON).body(request.toString()).when().put(url+"/"+bookId)
                .then().log().all()
                .assertThat().statusCode(HttpStatus.SC_BAD_REQUEST).extract().asPrettyString();

        Assertions.assertFalse(jsonString.contains(putAuthor));
        Assertions.assertFalse(jsonString.contains(putName));
        Assertions.assertFalse(jsonString.contains(String.valueOf(putYear)));
        Assertions.assertFalse(jsonString.contains(putIsElectronicBook));
        Assertions.assertTrue(jsonString.contains("\"error\": \"IsElectronicBook must be Bool type\""));
    }

    @Test
    public void A24PUTExistingBookInfoAuthorAsInteger() {

        int bookId = 2;
        String putName = "Отцы и дети";
        int putAuthor = 9999;
        int putYear = 4444;
        boolean putIsElectronicBook = true;

        JSONObject request = new JSONObject();
        request.put("name", putName);
        request.put("author", putAuthor);
        request.put("year", putYear);
        request.put("isElectronicBook", putIsElectronicBook);

        String jsonString = given().header("Content-Type", "application/json").contentType(ContentType.JSON)
                .accept(ContentType.JSON).body(request.toString()).when().put(url+"/"+bookId)
                .then().log().all()
                .assertThat().statusCode(HttpStatus.SC_BAD_REQUEST).extract().asPrettyString();

        Assertions.assertFalse(jsonString.contains(String.valueOf(putAuthor)));
        Assertions.assertFalse(jsonString.contains(putName));
        Assertions.assertFalse(jsonString.contains(String.valueOf(putYear)));
        Assertions.assertFalse(jsonString.contains(String.valueOf(putIsElectronicBook)));
        Assertions.assertTrue(jsonString.contains("\"error\": \"Author must be String type (Unicode)\""));
    }

    @Test
    public void A25PUTExistingBookInfoYearAsString() {

        int bookId = 2;
        String putName = "Властелин колец";
        String putAuthor = "Толкиен";
        String putYear = "пять";
        boolean putIsElectronicBook = true;

        JSONObject request = new JSONObject();
        request.put("name", putName);
        request.put("author", putAuthor);
        request.put("year", putYear);
        request.put("isElectronicBook", putIsElectronicBook);

        String jsonString = given().header("Content-Type", "application/json").contentType(ContentType.JSON)
                .accept(ContentType.JSON).body(request.toString()).when().put(url+"/"+bookId)
                .then().log().all()
                .assertThat().statusCode(HttpStatus.SC_BAD_REQUEST).extract().asPrettyString();

        Assertions.assertFalse(jsonString.contains(putAuthor));
        Assertions.assertFalse(jsonString.contains(putName));
        Assertions.assertFalse(jsonString.contains(putYear));
        Assertions.assertFalse(jsonString.contains(String.valueOf(putIsElectronicBook)));
        Assertions.assertTrue(jsonString.contains("\"error\": \"Year must be Int type\""));
    }

    @Test
    public void A26PUTMissingBookInfo() {

        int bookId = 720;
        String putName = "Хоббит";
        String putAuthor = "Tolkien";
        int putYear = 2222;
        boolean putIsElectronicBook = false;

        JSONObject request = new JSONObject();
        request.put("name", putName);
        request.put("author", putAuthor);
        request.put("year", putYear);
        request.put("isElectronicBook", putIsElectronicBook);

        String jsonString = given().header("Content-Type", "application/json").contentType(ContentType.JSON)
                .accept(ContentType.JSON).body(request.toString()).when().put(url+"/"+bookId)
                .then().log().all()
                .assertThat().statusCode(HttpStatus.SC_NOT_FOUND).extract().asPrettyString();

        Assertions.assertFalse(jsonString.contains(putAuthor));
        Assertions.assertFalse(jsonString.contains(putName));
        Assertions.assertFalse(jsonString.contains(String.valueOf(putYear)));
        Assertions.assertFalse(jsonString.contains(String.valueOf(putIsElectronicBook)));
        Assertions.assertTrue(jsonString.contains("\"error\": \"Book with id "+bookId+" not found\""));
    }

    @Test
    public void A27PUTExistingBookInfoOnlyYear() {
        int existingBook = 2;
        int putYear = 4444;
        JSONObject request = new JSONObject();
        request.put("year", putYear);

        String jsonString = given().header("Content-Type", "application/json").contentType(ContentType.JSON)
                .accept(ContentType.JSON).body(request.toString()).when().put(url+"/"+existingBook)
                .then().log().all()
                .assertThat().statusCode(HttpStatus.SC_BAD_REQUEST).extract().asPrettyString();

        Assertions.assertFalse(jsonString.contains(String.valueOf(putYear)));
        Assertions.assertTrue(jsonString.contains("\"error\": \"Name is required\""));
    }

    @Test
    public void A28PUTExistingBookNoParams() {
        JSONObject request = new JSONObject();
        int existingBook = 2;

        String jsonString = given().header("Content-Type", "application/json").contentType(ContentType.JSON)
                .accept(ContentType.JSON).body(request.toString()).when().put(url+"/"+existingBook)
                .then().log().all()
                .assertThat().statusCode(HttpStatus.SC_BAD_REQUEST).extract().asPrettyString();

        Assertions.assertTrue(jsonString.contains("\"error\": \"Not found request Json body\""));
    }

    @ParameterizedTest
    @ValueSource (ints = {0, 1, 5000, 9998, 9999})
    public void A29checkYearFieldLengthPositivePOST(Integer args) {

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Будущее");
        requestBody.put("year", args);
        RequestSpecification request1 = RestAssured.given();
        request1.header("Content-Type", "application/json");
        request1.body(requestBody.toString());
        Response response = request1.post(url);
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        assertThat(statusCode).isEqualTo(HttpStatus.SC_CREATED);
        System.out.println(statusDescr);
        response.body().prettyPrint();

    }

    @ParameterizedTest
    @ValueSource (ints = {-999,-1,10000,15000})
    public void A30checkYearFieldLengthNegative(Integer args) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Будущее");
        requestBody.put("year", args);
        RequestSpecification request1 = RestAssured.given();
        request1.header("Content-Type", "application/json");
        request1.body(requestBody.toString());
        Response response = request1.post(url);
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        assertThat(statusCode).isEqualTo(HttpStatus.SC_BAD_REQUEST);
        System.out.println(statusDescr);
        response.body().prettyPrint();

    }

    @ParameterizedTest
    @ValueSource (strings = {"", "206Ю", "206双", " 206", "20 6",
            "206 ", "206#", "206,", "206."})
    public void A31checkYearFieldLengthNegativeStringsPOST(String args) {

        String putName = "Будущее";
        JSONObject request = new JSONObject();
        request.put("name", putName);
        request.put("year", args);


        String jsonString = given().header("Content-Type", "application/json").contentType(ContentType.JSON)
                .accept(ContentType.JSON).body(request.toString()).when().post(url)
                .then().log().all()
                .assertThat().statusCode(HttpStatus.SC_BAD_REQUEST).extract().asPrettyString();

       Assertions.assertTrue(jsonString.contains("\"error\": \"Year must be Int type\""));
    }


    @ParameterizedTest
    @ValueSource (strings = {"К", "Кн", "КнигаКнигаКнигаКнигаКнига",
            "КнигаКнигаКнигаКнигаКнигККнигаКнигаКнигаКнигаКниг",
            "КнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнига", "Книга Книга",
            "Книга2549", "Книга-Книга", "Книга.Книга", "Книга,Книга", "BookBook", "书书书书书书书", "Книга*%"})
    public void A32checkBookNameFieldInputPositivePOST(String args) {

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", args);
        RequestSpecification request1 = RestAssured.given();
        request1.header("Content-Type", "application/json");
        request1.body(requestBody.toString());
        Response response = request1.post(url);
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        assertThat(statusCode).isEqualTo(HttpStatus.SC_CREATED);
        System.out.println(statusDescr);
        response.body().prettyPrint();
    }


    @ParameterizedTest
    @ValueSource (strings = {"", " ", "КнигаКнигаКнигаКнигаКнигККнигаКнигаКнигаКнигаКнигаК",
            "КнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнига", "КнигаКнигаКнигаКнигаКн" +
            "игаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнига" +
            "КнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКниг" +
            "аКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнигаКнига" +
            "КнигаКниг", " КнигаКнига", "КнигаКнига "})
    public void A33checkBookNameFieldInputNegativePOST(String args) {

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", args);
        RequestSpecification request1 = RestAssured.given();
        request1.header("Content-Type", "application/json");
        request1.body(requestBody.toString());
        Response response = request1.post(url);
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        assertThat(statusCode).isEqualTo(HttpStatus.SC_BAD_REQUEST);
        System.out.println(statusDescr);
        response.body().prettyPrint();
    }

    @ParameterizedTest
    @ValueSource (strings = {"", "И", "ИвановИвановИвановИвановИ", "ИвановИвановИИвановИвановИвановИвановИИвановИвано",
            "ИвановИвановИИвановИвановИвановИвановИИвановИванов", "Иванов Иванов", "Иванов-Иванов", "Иванов.Иванов",
            "Иванов,Иванов", "SmithSmith", "老子老子老子老子", "Иванов999", })
    public void A34checkAuthorFieldInputPositivePOST(String args) {

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Hello");
        requestBody.put("author", args);
        RequestSpecification request1 = RestAssured.given();
        request1.header("Content-Type", "application/json");
        request1.body(requestBody.toString());
        Response response = request1.post(url);
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        assertThat(statusCode).isEqualTo(HttpStatus.SC_CREATED);
        System.out.println(statusDescr);
        response.body().prettyPrint();
    }

    @ParameterizedTest
    @ValueSource (strings = {" ", "ИвановИвановИИвановИвановИвановИвановИИвановИвановИ",
            "ИвановИвановИИвановИвановИвановИвановИИвановИвановИааааааааа", " ИвановИванов", "ИвановИванов ",
            "Иванов№%", "ИвановИвановИИвановИвановИвановИвановИИвановИвановИаааааааааИвановИвановИИвановИванов" +
            "ИвановИвановИИвановИвановИаааааааааИвановИвановИИвановИвановИвановИвановИИвановИвановИаааааааааИван" +
            "овИвановИИвановИвановИвановИвановИИвановИвановИааааааааа"})

    public void A35checkAuthorNameFieldLengthAndInputNegativePOST(String args) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", args);
        requestBody.put("year", 1995);
        RequestSpecification request1 = RestAssured.given();
        request1.header("Content-Type", "application/json");
        request1.body(requestBody.toString());
        Response response = request1.post(url);
        int statusCode = response.getStatusCode();
        String statusDescr = response.getStatusLine();
        assertThat(statusCode).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @ParameterizedTest
    @ValueSource (ints = {01, 1, 2})
    public void A36checkBookIdInputPositive200GET(Integer args) {

            given()
                    .when()
                    .contentType(ContentType.JSON)
                    .get(url+"/"+args)
                    .then()
                    .assertThat().statusCode(HttpStatus.SC_OK)
                    .log()
                    .all();
    }


    @ParameterizedTest
    @ValueSource (ints = {1147483647, 2147483646, 2147483647})
    public void A37checkBookIdInputPositive404GET(Integer args) {

        given()
                .when()
                .contentType(ContentType.JSON)
                .get(url+"/"+args)
                .then()
                .assertThat().statusCode(HttpStatus.SC_NOT_FOUND)
                .log()
                .all();
    }

    @ParameterizedTest
    @ValueSource (strings = {"-10", "0", "2147483648", "1Ю", "1双", " 1", "1 ", "1#", "1，", "1.", " ", "33333333333333333333333888" +
            "838888333333333333333333333333333333333333333333333333333333333333333333" +
            "3333333333333333333333333333333333333333333333333333333333333333333333" +
            "3333333333333333333333333333333333333333333333333333333333333333333333" +
            "33333333333333333333333333333333333333333333333333333333333333333333333" +
            "33333333333333333333333333333333333333333333333333333333333333333333333" +
            "33333333333333333333333333333355555555555555555555555555555555555555555" +
            "555555555555555555555555555555555555555555555555555555555555555555555555" +
            "5555555555555555555555555555555555555555555556435645334564356435645364356" +
            "4536453654364356451111111111111111111111111111111111111111111111111111111" +
            "1111111111111111111111111111111111111111111111111111111111111111111113643564" +
            "3565436453643563465345643564356435643564533456435643564536435645364536543643" +
            "5645364356435654364536435634653456435643564356435645334564356435645364356453" +
            "6453654364356453643564356543645364356346534564356435643564356453345643564356" +
            "453643564536453654364356453643564356543645364356346534564356435643564356453" +
            "3456435643564536435645364536543643564536435643565436453643563465345643564356" +
            "43564356453345643564356453643564536453654364356453643564356543645364356346534" +
            "56435643564356435645334564356435645364356453645365436435645364356435654364536" +
            "435634653456435643564356435645334564356435645364356453645365436435645364356435" +
            "654364536435634653456435643564356435645334564356435645364356453645365436435645" +
            "3643564356543645364356346534564356435643564356453345643564356453643564536453654" +
            "3643564536435643565436453643563465345643564356435643564533456435643564536435645" +
            "3645365436435645364356435654364536435634653456435643564356435645334564356435645" +
            "36435645364536543643564536435643565436453643563465345643564356435643564533456435" +
            "64356453643564536453654364356453643564356543645364356346534564356435643564356453" +
            "34564356435645364356453645365436435645364356435654364536435634653456435643564356" +
            "435645334564356435645364356453645365436435645364356435654364536435634653456435643" +
            "564356435645334564356435645364356453645365436435645364356435654364536435634653456" +
            "4356435643564356453345643564356453643564536453654364356453643564356543645364356346" +
            "5345643564356435643564533456435643564536435645364536543643564536435643565436453643" +
            "56346534564356435643564356453345643564356453643564536453654364356453643564356543645" +
            "364356346534564356435643564356453345643566666666666666666666666666666666666666666666" +
            "66666666666666666666666666666666666666666666666666666666666666666666666666666666666" +
            "66666666666666666666666666666666666666666666666666666666666666666666666666666666666666" +
            "6666666777777777777777777777777777777777777777777777777777777777777777777777777777777" +
            "77777777777777777777777777777777777777777777777777777777777777777777777777777777777777" +
            "777777777777777777777777777777777777777777777777777777777777777777777777777777777777777" +
            "777777777777788888888888888888888888888888888888888888888888888888888888888888888888888" +
            "888888888888888888888888888888888888888888888888888888888888888888888888888888888888888" +
            "8888888888888888888888888888888888888888888888888888888888888888888888888888888888888888" +
            "888888888888888888888888888888888888888888888888888888888888888888888888888888888888888" +
            "88888111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
            "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
            "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
            "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
            "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
            "111111111111111111111111111111119999999999999999999999999999999999999999999999999999999" +
            "999999999999999999999999999999999999999999999999999999999999999999999999999999999999999" +
            "999999999999999999999999999999999999999999999999999999999999999999999999999999999999999" +
            "999999999999999991111111111111111111111111111111119999999999999999999999999999999999999" +
            "99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999" +
            "99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999" +
            "9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999" +
            "9999999999999999999999999999999999999999999999999999999999999999999999999999"})
    public void A38checkBookIdInputNegativeBADREQUEST(String args) {

        given()
                .when()
                .contentType(ContentType.JSON)
                .get(url+"/"+args)
                .then()
                .assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .log()
                .headers();
    }
}
