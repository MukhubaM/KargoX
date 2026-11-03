import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

let client = null

function getClient() {
  if (client) return client
  client = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    reconnectDelay: 4000,
  })
  client.activate()
  return client
}

// Subscribes to a STOMP topic once the connection is ready and returns an unsubscribe function. I used this for live job offers, booking status pushes and in-app notifications
export function subscribeTopic(topic, onMessage) {
  const c = getClient()
  let sub = null

  const doSubscribe = () => {
    sub = c.subscribe(topic, (message) => {
      try {
        onMessage(JSON.parse(message.body))
      } catch {
        onMessage(message.body)
      }
    })
  }

  if (c.connected) {
    doSubscribe()
  } else {
    c.onConnect = doSubscribe
  }

  return () => sub?.unsubscribe()
}
