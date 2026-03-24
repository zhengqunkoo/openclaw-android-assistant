<template>
  <div class="terminal-pane">
    <template v-if="!terminalSrc">
      <div class="terminal-no-url">
        <p class="terminal-no-url-message">No terminal URL configured.</p>
        <p class="terminal-no-url-hint">
          Go to
          <button class="terminal-settings-link" type="button" @click="$emit('go-to-settings')">Settings</button>
          and set the ttyd URL.
        </p>
      </div>
    </template>
    <iframe
      v-else
      :src="terminalSrc"
      class="terminal-iframe"
      allow="clipboard-read; clipboard-write"
      title="Terminal"
    />
  </div>
</template>

<script setup lang="ts">
import { useTerminalSettings } from '../../composables/useTerminalSettings'

defineEmits<{
  (e: 'go-to-settings'): void
}>()

const { ttydUrl: terminalSrc } = useTerminalSettings()
</script>

<style scoped>
@reference "tailwindcss";

.terminal-pane {
  @apply flex-1 min-h-0 w-full h-full flex flex-col;
}

.terminal-iframe {
  @apply flex-1 min-h-0 w-full h-full border-0;
}

.terminal-no-url {
  @apply flex-1 min-h-0 flex flex-col items-center justify-center gap-2 text-sm text-zinc-500 px-6 text-center;
}

.terminal-no-url-message {
  @apply m-0 font-medium text-zinc-700;
}

.terminal-no-url-hint {
  @apply m-0;
}

.terminal-settings-link {
  @apply underline text-zinc-700 hover:text-zinc-900 bg-transparent border-0 p-0 cursor-pointer;
}
</style>
