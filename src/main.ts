import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Vant from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'
import router from './router'
import './style.css'
import { useAudioStore } from './stores/audio'

const app = createApp(App)
const pinia = createPinia()

app.use(router)
app.use(Vant)
app.use(pinia)

const audioStore = useAudioStore(pinia)
audioStore.initAudio()

app.mount('#app')
