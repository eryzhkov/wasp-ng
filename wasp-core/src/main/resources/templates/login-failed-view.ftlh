<#import "/spring.ftl" as spring>

<!doctype html>
<html lang="en">
<head>
  <title>WASP/NextGen :: Login Failed</title>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<!-- Navigation bar -->
<nav class="navbar navbar-expand-sm bg-primary navbar-dark">
  <div class="container-fluid">
    <div class="navbar-brand">WASP</div>
  </div>
</nav>
<!-- Main content -->
<div class="container-md pt-5">
  <div class="row">
    <div class="col-md-6 offset-md-3 alert alert-danger text-center">
      <p><strong>Login failed!</strong> <a href="<@spring.url "/login"/>">Tray again</a>.</p>
    </div>
  </div>
</div>
</body>
</html>