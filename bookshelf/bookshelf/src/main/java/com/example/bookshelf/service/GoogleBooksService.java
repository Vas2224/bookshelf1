package com.example.bookshelf.service;

import com.example.bookshelf.model.Book;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleBooksService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${google.books.api.url}")
    private String googleBooksApiUrl;

    public GoogleBooksService(ObjectMapper objectMapper) {
        this.webClient = WebClient.create();
        this.objectMapper = objectMapper;
    }

    public List<Book> searchBook(String query) {
        String apiUrl = googleBooksApiUrl + "?q=" + query;

        String jsonResponse = webClient.get()
                .uri(apiUrl)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just("{}"))
                .block();

        List<Book> searchResults = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode items = root.path("items");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    JsonNode volumeInfo = item.path("volumeInfo");

                    Book book = new Book();
                    book.setTitle(volumeInfo.path("title").asText());

                    JsonNode authors = volumeInfo.path("authors");
                    if (authors.isArray()) {
                        List<String> authorList = new ArrayList<>();
                        authors.forEach(a -> authorList.add(a.asText()));
                        book.setAuthor(String.join(", ", authorList));
                    }

                    JsonNode industryIdentifiers = volumeInfo.path("industryIdentifiers");
                    if (industryIdentifiers.isArray()) {
                        for (JsonNode idNode : industryIdentifiers) {
                            if ("ISBN_13".equals(idNode.path("type").asText())) {
                                book.setIsbn(idNode.path("identifier").asText());
                                break;
                            }
                        }
                    }

                    JsonNode imageLinks = volumeInfo.path("imageLinks");
                    if (imageLinks.isObject()) {
                        book.setCoverImageUrl(imageLinks.path("thumbnail").asText(null));
                    }

                    book.setPageCount(volumeInfo.path("pageCount").asInt());
                    book.setRead(false);

                    searchResults.add(book);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return searchResults;
    }
}
