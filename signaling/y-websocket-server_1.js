const WebSocket = require('ws')
const http      = require('http')
const { setupWSConnection } = require('y-websocket/bin/utils')

const PORT   = 1234
const server = http.createServer((req, res) => {
  res.writeHead(200)
  res.end('Y.js WebSocket Server Running')
})

const wss = new WebSocket.Server({ server })

wss.on('connection', (ws, req) => {
  // setupWSConnection 自动处理：
  //   房间管理（URL path 作为房间名，对应 docId）
  //   Y.js 文档状态持久化（内存，重启清空）
  //   Awareness 广播
  //   断线重连
  setupWSConnection(ws, req)
})

server.listen(PORT, () => {
  console.log(` Y.js WebSocket 服务已启动: ws://localhost:${PORT}`)
  console.log(` 每个文档通过 docId 区分房间，自动管理协同状态`)
})