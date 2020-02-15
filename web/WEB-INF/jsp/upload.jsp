<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="false" %>
<html>
<head>
    <title>Upload File Request Page</title>
</head>
<body>
<form method="POST" action="uploadFile" enctype="multipart/form-data" >
    File to upload: <input type="file" name="fileList" multiple>
    <p></p>
    Press here to upload the file: <input type="submit" value="Upload">
</form>

</body>
</html>