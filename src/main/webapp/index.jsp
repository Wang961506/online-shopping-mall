<html>
<body>
<h2>Hello World!</h2>

<h4>上传文件</h4>
<form name="form1" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" value="upload">
</form>

<h4>富文本图片上传文件</h4>
<form name="form1" action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" value="upload">
</form>
</body>
</html>
