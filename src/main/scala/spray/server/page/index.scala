package spray.server.page

/**
 * @author vermisse
 */
object index {
  def apply() = {
    <html>
      <head>
        <meta charset="utf-8"/>
        <title>标题</title>
      </head>
      <body>
        <h1>Spray框架Demo</h1>
        <p>Defined resources:</p>
        <ul>
          <li><a href="ping">ping</a></li>
          <li><a href="stream">stream</a></li>
          <li><a href="crash">crash</a></li>
          <li><a href="timeout">timeout</a></li>
          <li><a href="timeout/timeout">timeout/timeout</a></li>
        </ul>
      </body>
    </html>
  }
}