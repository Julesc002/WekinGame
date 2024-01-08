package com.wekinGames.Controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wekinGame.Controllers.CategoryController;
import com.wekinGame.Repository.WikiRepository;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

	MockedStatic<WikiRepository> wikiMock = mockStatic(WikiRepository.class);

    @InjectMocks
	CategoryController categoryController;

	//@BeforeEach
	//public void init() {
	//	classUnderTest = new CalculatorServiceImpl(calculator, solutionFormatter);
	//}
	
    @Test
    public void testGetCategoriesWithIdWiki() {
        // GIVEN
        String idWiki = "1";
        List<String> expectedCategories = Arrays.asList("categorie1", "categorie2");
        Document expectedWiki = new Document();
        expectedWiki.append("_id", idWiki);
        expectedWiki.append("categories", expectedCategories);
        wikiMock.when(() -> WikiRepository.getById(Integer.parseInt(idWiki))).thenReturn(expectedWiki);
        
        // WHEN
        List<String> obtainedCategories = categoryController.getCategoriesWithIdWiki(idWiki);

        // THEN
        assertEquals(expectedCategories, obtainedCategories);
    }

}