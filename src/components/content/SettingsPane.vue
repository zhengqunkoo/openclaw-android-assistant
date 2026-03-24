<template>
  <div class="settings-pane">
    <section class="settings-section">
      <h2 class="settings-section-title">Terminal (ttyd)</h2>

      <div class="settings-field">
        <label class="settings-label" for="ttyd-url">ttyd Base URL</label>
        <input
          id="ttyd-url"
          v-model="ttydBaseUrl"
          class="settings-input"
          type="url"
          placeholder="http://localhost:7681/"
          spellcheck="false"
          autocomplete="off"
        />
        <p class="settings-hint">
          The URL where your ttyd server is running. Leave empty to disable the Terminal pane.
        </p>
      </div>

      <div class="settings-field">
        <label class="settings-label">URL Parameters</label>
        <p class="settings-hint">
          Key=value pairs appended to the ttyd URL as query string parameters.
        </p>
        <div class="settings-params-list">
          <div
            v-for="(param, index) in ttydParams"
            :key="index"
            class="settings-param-row"
          >
            <input
              v-model="param.key"
              class="settings-input settings-param-input"
              type="text"
              placeholder="key"
              spellcheck="false"
              autocomplete="off"
            />
            <span class="settings-param-eq">=</span>
            <input
              v-model="param.value"
              class="settings-input settings-param-input"
              type="text"
              placeholder="value"
              spellcheck="false"
              autocomplete="off"
            />
            <button
              class="settings-param-remove"
              type="button"
              aria-label="Remove parameter"
              @click="removeParam(index)"
            >
              ✕
            </button>
          </div>
        </div>
        <button class="settings-add-param" type="button" @click="addParam">
          + Add parameter
        </button>
      </div>

      <div v-if="previewUrl" class="settings-field">
        <label class="settings-label">Preview URL</label>
        <code class="settings-preview-url">{{ previewUrl }}</code>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useTerminalSettings, buildTtydUrl } from '../../composables/useTerminalSettings'
import type { TtydParam } from '../../composables/useTerminalSettings'

const { ttydBaseUrl, ttydParams } = useTerminalSettings()

function addParam(): void {
  ttydParams.value.push({ key: '', value: '' })
}

function removeParam(index: number): void {
  ttydParams.value.splice(index, 1)
}

const previewUrl = computed(() => buildTtydUrl(ttydBaseUrl.value, ttydParams.value as TtydParam[]))
</script>

<style scoped>
@reference "tailwindcss";

.settings-pane {
  @apply flex-1 min-h-0 overflow-y-auto px-6 py-4 flex flex-col gap-6;
}

.settings-section {
  @apply flex flex-col gap-4 max-w-xl;
}

.settings-section-title {
  @apply m-0 text-base font-semibold text-zinc-900;
}

.settings-field {
  @apply flex flex-col gap-1.5;
}

.settings-label {
  @apply text-sm font-medium text-zinc-700;
}

.settings-hint {
  @apply m-0 text-xs text-zinc-500;
}

.settings-input {
  @apply w-full rounded-md border border-zinc-300 bg-white px-3 py-1.5 text-sm text-zinc-900
         placeholder-zinc-400 outline-none transition focus:border-zinc-500 focus:ring-1 focus:ring-zinc-500;
}

.settings-params-list {
  @apply flex flex-col gap-2;
}

.settings-param-row {
  @apply flex items-center gap-1.5;
}

.settings-param-input {
  @apply flex-1 min-w-0;
}

.settings-param-eq {
  @apply text-sm text-zinc-400 shrink-0;
}

.settings-param-remove {
  @apply h-7 w-7 shrink-0 flex items-center justify-center rounded-md border border-transparent
         text-zinc-400 transition hover:border-zinc-200 hover:bg-zinc-100 hover:text-zinc-600
         bg-transparent text-xs cursor-pointer;
}

.settings-add-param {
  @apply self-start rounded-md border border-zinc-300 bg-white px-3 py-1.5 text-sm text-zinc-600
         transition hover:bg-zinc-50 hover:border-zinc-400 cursor-pointer;
}

.settings-preview-url {
  @apply block rounded-md bg-zinc-100 border border-zinc-200 px-3 py-2 text-xs text-zinc-700
         break-all font-mono;
}
</style>
