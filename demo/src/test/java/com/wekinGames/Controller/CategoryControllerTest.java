package com.wekinGames.Controller;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {
/**
	@Mock
	MongoDatabase database;

    @InjectMocks
	CategoryController categoryController;

	//@BeforeEach
	//public void init() {
	//	classUnderTest = new CalculatorServiceImpl(calculator, solutionFormatter);
	//}
	
    @Test
    public void testGetCategoryWithIdWiki() {
        // GIVEN
        String idWiki = "1";
        MongoCollection<Document> wikis = new MongoCollection<Document>();
        when(database.getCollection("wikis")).thenReturn(null);
        List<String> expectedCategory = Arrays.asList("Category1", "Category2");
        
        // WHEN
        categoryController.getCategoryWithIdWiki(idWiki);
        assertEquals(categoryController.getCategoryWithIdWiki(idWiki), expectedCategory);

        // THEN
    } */

}