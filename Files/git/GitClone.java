import org.json.*;

public class GitClone {

  public static String context;

  public static final String url = "https://api.github.com/repos/";

  public String path;
  private String owner;
  private String repo;
  private String token;
  private File file;

  public GitClone(String path, String owner, String repo, String token) {
    this.path = path;
    this.owner = owner;
    this.repo = repo;
    this.token = token;
    this.file = new File(context, path);
  }

  public void commit() {
    HttpURLConnection connection = null;
    BufferedReader br = null;

    try {
      String apiUrl = url + owner + "/" + repo + "/contents/" + path;
      connection = (HttpURLConnection) new URL(apiUrl).openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
      connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
      connection.setRequestProperty("Authorization", "Bearer " + token);

      int responseCode = connection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
          response.append(responseLine.trim());
        }

        JSONObject fileObject = new JSONObject(response.toString());
        String fileName = fileObject.getString("name");
        String encodedContent = fileObject.getString("content");

        Console.log(fileName);

        byte[] decodedContent = new byte[] {0};
        decodedContent = Base64.getDecoder().decode(encodedContent.trim());
        Console.log(new String(decodedContent));
        saveFile(fileName, decodedContent);
      }  else {
        br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        StringBuilder errorResponse = new StringBuilder();
        String errorLine;
        while ((errorLine = br.readLine()) != null) {
          errorResponse.append(errorLine.trim());
        }
        Console.log("Erro ao clonar arquivo: " + errorResponse.toString());
        Console.log(apiUrl);
      }

    } catch (IOException e) {
      Console.log(e);
    } catch (JSONException e) {
      Console.log(e);
    } finally {
      try {
        if (br != null) br.close();
        if (connection != null) connection.disconnect();
      } catch (IOException ex) {
        Console.log(ex);
      }
    }
  }

  public void setOutputFile(File file) {
    this.file = file;
  }

  private void saveFile(String fileName, byte[] content) {
    try {
      if (!file.exists()) {
        file.getParentFile().mkdirs();
        file.createNewFile();
      }
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(content);
      fos.flush();
      fos.close();
    } catch (IOException e) {
      Console.log(e);
    }
  }
}
