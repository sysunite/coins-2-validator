Promise        =        require('bluebird')
mysql          =        require('mysql-promise')
express        =        require("express")
xmlparser      =        require('express-xml-bodyparser')
app            =        express()


checkInitialized = ->
  return Promise.resolve(true) if initConfirmed
  console.log("Trying to connect to registry database")
  delay = 5000

  db.query('USE `registry`;').then(->
    db.query('SELECT * FROM `registry` LIMIT 1;')
  ).then(->
    initConfirmed = true
  ).catch((error)->
    if error.code is 'ER_NO_SUCH_TABLE'
      false
    else
      if error.code is 'ER_BAD_DB_ERROR'
        db.query('CREATE DATABASE `registry`;').then(->checkInitialized())
      if tries-- > 0
        Promise.delay(delay).then(->checkInitialized())
      else
        Promise.reject("Could not connect to mysql database, #{error.code}.")
  )



initDb = ->
  checkInitialized().then((done)->
    return Promise.resolve() if done
    console.log "Dropping and creating table"
    db.query('DROP TABLE IF EXISTS `registry`;')
    .then(->
      console.log "Creating table"
      db.query('
        CREATE TABLE `registry` (
        `seqnr` BIGINT NOT NULL AUTO_INCREMENT,
        `timestamp` BIGINT  NOT NULL,
        `zooi` varchar(128) NOT NULL,
        `xml` text NOT NULL,
        PRIMARY KEY (`seqnr`),
        INDEX `timestamp_index` (`timestamp` ASC),
        INDEX `zooi_index` (`zooi` ASC)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8;'
      )
    ).then(->
      initConfirmed = true
    )
  )

processWrites = (xml)->
  initDb().then(->
    console.log(Date.now() + " Processing XML")
#    console.log(xml)
#      if not writes? or writes.length < 1
#        return Promise.resolve()
#
    query = 'INSERT INTO `registry` (`timestamp`, `zooi`, `xml`) VALUES '
    query += "(#{Date.now()}, #{db.pool.escape(xml.zooi)}, #{db.pool.escape(JSON.stringify(xml))});"

    db.query(query)
  )


tries = 10
delay = 5000
db = mysql()
initConfirmed = false
db.configure({
  host     : 'registry-database'
  port     : '3306'
  user     : 'registry'
  password : 'vZGZww8HLWTLqnPY'
  dateStrings: true # force dates as string, no javascript date
})

app.use(xmlparser())

app.post('/report',(req, res, next)->
  processWrites(req.body).then(->
    res.send('')
  )
)
app.listen(39444,->
  console.log("Started on PORT 39444")
)



