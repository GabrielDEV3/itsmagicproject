import org.json.*;

public class GitPush {

  public static String context;
  public static final String url = "https://api.github.com/repos/";
  private static final String extension = ".git";
  private String path;
  private File file;
  private String owner;
  private String repo;
  private String token;

  public GitPush(String path, String owner, String repo, String token) {
    this.path = path;
    this.file = new File(context, path);
    this.owner = owner;
    this.repo = repo;
    this.token = token;
  }

  private String encodeFileToBase64() throws IOException {
    FileInputStream fis = null;
    ByteArrayOutputStream baos = null;

    try {
      fis = new FileInputStream(file);
      baos = new ByteArrayOutputStream();

      byte[] buffer = new byte[1024];
      int bytesRead;

      while ((bytesRead = fis.read(buffer)) != -1) {
        baos.write(buffer, 0, bytesRead);
      }

      byte[] fileContent = baos.toByteArray();
      return Base64.getEncoder().encodeToString(fileContent);

    } finally {
      if (fis != null) {
        fis.close();
      }
      if (baos != null) {
        baos.close();
      }
    }
  } 

  public void commit(String commitMessage) {
    HttpURLConnection connection = null;
    OutputStream os = null;
    BufferedReader br = null;

    try {
      String encodedContent = encodeFileToBase64();

      String apiUrl = url + owner + "/" + repo + "/contents/" + path;
      connection = (HttpURLConnection) new URL(apiUrl).openConnection();
      connection.setRequestMethod("PUT");
      connection.setRequestProperty("Authorization", "Bearer " + token);
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true);

      JSONObject jsonBody = new JSONObject();
      jsonBody.put("message", commitMessage);
      jsonBody.put("content", encodedContent);

      os = connection.getOutputStream();
      os.write(jsonBody.toString().getBytes("utf-8"));

      br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
      StringBuilder response = new StringBuilder();
      String responseLine;
      while ((responseLine = br.readLine()) != null) {
        response.append(responseLine.trim());
      }
      Console.log("Resposta do GitHub: " + response.toString());

    } catch (IOException e) {
      Console.log(e);
    } catch (JSONException e) {
      Console.log(e);
    } finally {
      try {
        if (os != null) os.close();
        if (br != null) br.close();
        if (connection != null) connection.disconnect();
      } catch (IOException ex) {
        Console.log(ex);
      }
    }
  }
}
