 package cs1302.gallery;   

import javafx.scene.layout.HBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.scene.control.ProgressBar;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import java.util.Arrays;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.layout.TilePane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.animation.Animation.Status;
 
/**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
* This is a custom component to simplify the                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
* code called in GalleryApp.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
*/
public class GalleryLoader extends VBox {
 
   protected Button playButton;
   protected Label searchLabel;
   protected TextField queryTermField;
   protected ComboBox queryDropDown;
   protected Button getImagesButton;
   protected HBox hboxField;
   protected HBox statusField;
   protected HBox loadBarBox;
   protected ProgressBar bar;
   protected Text loadText = new Text("Images provided by iTunes Search API.");
   protected HBox initialImage;
   protected HBox imageHolder;
   protected String[] imageURLArray;
   protected int size = 0;
   protected String specificity = "music";
   protected String url;
   protected Text statusText;
   protected TilePane imagePane = new TilePane();
   protected Double progress = 0.0;
   protected ImageView[] images = new ImageView[20];
   protected Boolean play = true;
   protected double playCount = -1.0;
   protected Timeline timeline;
   protected String[] genArray;
   protected String[] unusedURLArray;
   private static final String DEFAULT_IMG =
   "https://www.tileshackdirect.co.uk/images/detailed/2/PC135-1010-resize.jpg";
   private static final double DEF_HEIGHT = 102;
   private static final double DEF_WIDTH = 100;
   
   public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
  
     /** Google {@code Gson} object for parsing JSON-formatted strings. */
      public static Gson GSON = new GsonBuilder()
           .setPrettyPrinting()
           .create();
   
       private static final String ITUNES_API = "https://itunes.apple.com/search";
   
       /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
        * This is a very simple constructor to make the correct node objects and put                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
        * them in the correct order.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
        */
       public GalleryLoader() {
           this.setSpacing(4.0);
           this.getChildren().addAll(makeSearchField(), makeStatusField(), makeInitial(),
               makeLoadField());
       } //searchFieldBox                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * This method creates the searchField HBox.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
       *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
       * @return searchField a HBox creation of the searchField.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
       */
      public HBox makeSearchField() {
          playButton = new Button("Play");
          playButton.setDisable(true);
          searchLabel = new Label("Search:");
          queryTermField = new TextField("");
          queryTermField.setPrefWidth(175);
          queryDropDown = new ComboBox();
          queryDropDown.setMinWidth(100.0);
          queryDropDown.setMaxWidth(100.0);
          getImagesButton = new Button("Get Images");
          getImagesButton.setMinWidth(100.0);
          getImagesButton.setMaxWidth(100.0);
          searchLabel.setMaxHeight(50.0);
          setQueryBox();
          getImagesButton.setOnAction(event -> {
              loadImages();
              boolean wasRunning = false;
              if (timeline != null) {
                  if (timeline.getStatus() == Status.RUNNING) {
                      wasRunning = true;
                      timeline.pause();
                  } //if                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
              } //if                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
              if (wasRunning) {
                  timeline.play();
              } //if                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
          });
          playButton.setOnAction(play -> imageChange());
          HBox searchField = new HBox();
          searchField.setSpacing(4.0);
          searchField.getChildren().addAll(playButton,searchLabel,
               queryTermField, queryDropDown, getImagesButton);
          HBox.setHgrow(queryTermField, Priority.ALWAYS);
         return searchField;
      } //makeSearchField                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * Creates the statusfield HBox.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
       *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
       * @return statusField a HBox creation of the statusField.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
       */
      public HBox makeStatusField() {
          statusText = new Text("Type in a term, select a media type, then click the button.");
          statusField = new HBox();
          statusField.getChildren().addAll(statusText);
          return statusField;
      } //makeStatusField                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * Creates an initial image that is used either when an error happens or when the                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
       * program is started.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   
       *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
       * @return loadBarBox a HBox creation of the initial image.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
       */
      public HBox makeInitial() {
          initialImage = new HBox();
          Image imgDef = new Image(DEFAULT_IMG, 510, 400, false, false);
          ImageView imgViewDef = new ImageView(imgDef);
          imgViewDef.setPreserveRatio(true);
          initialImage.getChildren().addAll(imgViewDef);
          return initialImage;
      } //makeInitial                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * Creates the loadField HBox.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
       *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
       * @return loadBarBox the HBox creation of the loadField.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
       */
      public HBox makeLoadField() {
          loadBarBox = new HBox(4.0);
          bar = new ProgressBar();
          bar.setMinWidth(250.0);
          bar.setMaxWidth(250.0);
          bar.setProgress(0);
          loadBarBox.getChildren().addAll(bar, loadText);
          return loadBarBox;
      } //makeLoadField                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * Makes the runnable object for loading images when the load button is pressed.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
       */
      public void loadImages() {
          Runnable loadImages = () -> {
              try {
                  setStatusLoading();
                  size = 0;
                  genArray = new String[0];
                  //This code encode strings in order for the URL to work.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
                  String term = URLEncoder.encode(queryTermField.getText() , StandardCharsets.UTF_8);
                  String limit = URLEncoder.encode("200", StandardCharsets.UTF_8);
                  String spec = URLEncoder.encode(specificity, StandardCharsets.UTF_8);
                  String query = String.format("?term=%s&limit=%s&media=%s", term, limit, spec);
                  url = ITUNES_API + query;
                  HttpRequest request = HttpRequest.newBuilder()
                      .uri(URI.create(url))
                      .build();
  
                  HttpResponse<String> response = HTTP_CLIENT
                      .send(request, BodyHandlers.ofString());
  
                  ItunesResponse parsedResponse = GSON.fromJson(response.body(),
                      ItunesResponse.class);
  
                  genArray = new String[parsedResponse.resultCount];
  
                  resetBar();
                  for (ItunesResult res: parsedResponse.results) {
                      add(res.artworkUrl100);
                  } //for                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
                  if (checkResults()) {
                      makeArrays();
                  } //if                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
                  updateImages();
              } catch (IOException | InterruptedException x) {
                  System.out.println("ERROR: please try again.");
              } //try                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
          }; //loadImages                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
          runNow(loadImages);
      } //loadImages                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * Sets the statusField text to loading when the load button is pressed.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
       */
      public void setStatusLoading() {
        String load = "Getting Images...";
          Text loading = new Text(load);
          Runnable loadIt = () -> {
              playButton.setDisable(true);
              playButton.setText("Play");
              getImagesButton.setDisable(true);
              statusField.getChildren().clear();
              statusField.getChildren().add(loading);
          };
          Platform.runLater(loadIt);
      } //setStatusLoading                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * Sets the statusField text to the URL and then sets a new imagePane.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   
       */
      public void updateImages() {
          Runnable update = () -> {
              statusField.getChildren().clear();
              Text urlText = new Text(url);
              statusField.getChildren().add(urlText);
              this.getChildren().remove(2);
              this.getChildren().add(2, imageMaker());
              playButton.setDisable(false);
              getImagesButton.setDisable(false);
          }; //next                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
          Platform.runLater(update);
      } //updateImages                                                                                                                                                                      
  
      /**                                                                                                                                                                                   
       * Adds urls the the general array.                                                                                                                                                   
       *                                                                                                                                                                                    
       * @param artworkURL the string to the corresponding artworkURL.                                                                                                                      
       */
      public void add(String artworkURL) {
          try {
              imagePane.setPrefColumns(5);
              imagePane.setPrefRows(4);
              if (size == 0) {
                  genArray[0] = artworkURL;
                  Platform.runLater(() -> increaseProgress());
                  Thread.sleep(10);
                  size++;
              } else {
                  for (int i = 0; i < size; i++) {
                      //This code makes sure there arent any repated URLs                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
                      if (!(Arrays.asList(genArray).contains(artworkURL)) && (size < 200)) {
                          Platform.runLater(() -> increaseProgress());
                          Thread.sleep(10);
                          genArray[size] = artworkURL;
                          size++;
                      } //if                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   
                  } //for                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
              } //else                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
          } catch (InterruptedException e) {
              System.out.println("InterruptedException.");
          } //try                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
      } //add                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * Makes two arrays, one array for the used urls and oone for the                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
       * unused arrays.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
       */
      public void makeArrays() {
          imageURLArray = new String[0];
          unusedURLArray = new String[0];
          imageURLArray = new String[20];
          int value = Math.abs(size - 20);
          unusedURLArray = new String[value];
          for (int i = 0; i < 20; i++) {
              imageURLArray[i] = genArray[i];
         } //for                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
          for (int i = 20, j = 0; i < size; i++, j++) {
              unusedURLArray[j] = genArray[i];
          } //for                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
      } //makeArrays                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * Checks to make sure there are at least 21 unique urls.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
       *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
       * @return results the boolean if the results are valid or not.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
       */
      public Boolean checkResults() {
          Boolean results = true;
          if (genArray.length < 21) {
              results = false;
              Platform.runLater(() -> {
                  String failed = "Last attempt to get images failed...";
                  Text fail = new Text(failed);
                  statusField.getChildren().clear();
                  statusField.getChildren().add(fail);
                  String uri = "URI:" + url;
                  String except = "\n\nException:java.lang.IllegalArgumentException:"
                      + genArray.length + " distinct results found, but 21 or"
                      + " more are needed.\n\n\n\n";
                  Alert alert = new Alert(Alert.AlertType.ERROR,
                      uri + except , ButtonType.OK);
                  alert.getDialogPane().setMinWidth(600);
                  alert.showAndWait();
                  this.getChildren().remove(2);
                  this.getChildren().add(2, makeInitial());
                  statusField.getChildren().clear();
                  statusField.getChildren().add(statusText);
                  playButton.setText("Play");
              }); //runlater                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   
          } //if                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
        return results;
      } //checkResults                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * Creates the querybox and sets its elements.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
      */
      public void setQueryBox () {
          String[] choices = {"movie", "podcast", "music", "musicVideo", "audiobook", "shortFilm",
                              "tvShow", "software", "ebook", "all"};
          queryDropDown.getItems().addAll(FXCollections.observableArrayList(choices));
          queryDropDown.getSelectionModel().select(2);
          queryDropDown.setOnAction ((qdd) -> {
              String selectedItem = queryDropDown.getSelectionModel().getSelectedItem().toString();
              specificity = selectedItem;
          });
      } //setQueryBox                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * This is the only thread method that is called when loading in new images.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
       *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
       * @param target the runnable object to load images.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
       */
      public static void runNow(Runnable target) {
          Thread t = new Thread(target);
          t.setDaemon(true);
          t.start();
      } //runNow                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * Makes a tilepane full of images to post onto the window.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
       *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
       * @return imagePane the tilePane of images.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
       */
      public TilePane imageMaker() {
          int results = genArray.length;
          if (results < 21) {
              return imagePane;
          } //if                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
          imagePane.getChildren().clear();
          for (int i = 0; i < 20; i++) {
              images[i] = new ImageView();
              Image img = new Image(imageURLArray[i] ,DEF_HEIGHT
                  , DEF_WIDTH, false, false);
             images[i].setImage(img);
              imagePane.getChildren().add(images[i]);
          } //for                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
          return imagePane;
      } //images                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * Increases the progress of the progress bar.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
       */
      public void increaseProgress() {
          progress = progress + 0.05;
          bar.setProgress(progress);
      } //increaseProgress                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * Resets the progress bar.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
       */
      public void resetBar() {
          progress = 0.0;
          Runnable reset = () -> {
              bar.setProgress(0);
          }; //reset                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
          Platform.runLater(reset);
      } //resetBar                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * When called this method checks if the play button is                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
       * play or paused and then calls the right methods accordingly.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
      */
      public void imageChange() {
          playCount++;
          if (playCount % 2 == 0.0) {
              play = true;
          } //if                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
          if (playCount % 2 != 0.0) {
              play = false;
          } //if                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
          Platform.runLater(() -> {
              if (play) {
                  playButton.setText("Pause");
                  timeline.play();
              } //if                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
             if (!play) {
                  playButton.setText("Play");
                  timeline.pause();
                  return;
              } //if                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
          });
          if (play) {
              isPlay();
          } //if                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
      } //imageChange                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * If the timeline is set to play this method will get a                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
       * random image being displayed and then calls the randomImage                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
       * method.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
       */
      public void isPlay() {
          EventHandler<ActionEvent> playIt = (play -> {
              if (genArray.length > 21) {
                  if (playButton.getText() == "Play") {
                      timeline.pause();
                      return;
                  } //if                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
                  int usedInt = (int) (Math.random() * imageURLArray.length);
  
                  String usedString = imageURLArray[usedInt];
                  if (usedString != null) {
                      randomImage(usedString, usedInt);
                  } //if                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
              } //if                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
          });
          makeTimeline(playIt);
      } //isPlay                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * This method updates a random image from an unsed image.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
       *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
       * @param usedString the string of the url currently in use.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
       * @param usedInt the number of the image currently in use.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
       */
      public void randomImage(String usedString, int usedInt) {
          int unUsedInt = (int) (Math.random() * unusedURLArray.length);
          String unUsedString = unusedURLArray[unUsedInt];
          String temp = usedString;
          imageURLArray[usedInt] = unusedURLArray[unUsedInt];
          unusedURLArray[unUsedInt] = temp;
          updateImages();
      } //randomImage                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
  
      /**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
       * This method creates the timeline.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
       *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
       * @param handler the event that is caused by selecting the play button.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
       */
      public void makeTimeline(EventHandler<ActionEvent> handler) {
          KeyFrame keyframe = new KeyFrame(Duration.seconds(2), handler);
          timeline = new Timeline();
          timeline.setCycleCount(Timeline.INDEFINITE);
          timeline.getKeyFrames().add(keyframe);
          timeline.play();
      } //makeTimeline                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
  
  } //searchFieldBox                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
