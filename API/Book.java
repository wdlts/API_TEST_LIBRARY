package API;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Book {

    @JsonProperty("author")
    public String getAuthor() {
        return this.author; }
    public void setAuthor(String author) {
        this.author = author; }
    String author;

    @JsonProperty("id")
    public Integer getId() {
        return this.id; }
    public void setId(Integer id) {
        this.id = id; }
    Integer id;

    @JsonProperty("isElectronicBook")
    public Boolean getIsElectronicBook() {
        return this.isElectronicBook; }
    public void setIsElectronicBook(Boolean isElectronicBook) {
        this.isElectronicBook = isElectronicBook; }
    Boolean isElectronicBook;

    @JsonProperty("name")
    public String getName() {
        return this.name; }
    public void setName(String name) {
        this.name = name; }
    String name;

    @JsonProperty("year")
    public Integer getYear() {
        return this.year; }
    public void setYear(Integer year) {
        this.year = year; }
    Integer year;
}
