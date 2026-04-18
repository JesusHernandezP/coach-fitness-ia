import {
  Component, OnInit, AfterViewChecked, inject, signal,
  ViewChild, ElementRef,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { ChatService, Conversation, ChatMessage } from './chat.service';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [FormsModule, DatePipe],
  template: `
    <div class="chat-layout">

      <!-- ── Conversation sidebar ── -->
      <aside class="conv-sidebar">
        <div class="conv-header">
          <p class="sidebar-eyebrow">Conversaciones</p>
          <button class="btn-new-conv" (click)="newConversation()" title="Nueva conversación">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M7 2v10M2 7h10" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
          </button>
        </div>

        @if (loadingConvs()) {
          <div class="conv-loading">
            @for (i of [1,2,3]; track i) { <div class="conv-skeleton"></div> }
          </div>
        } @else if (conversations().length === 0) {
          <div class="conv-empty">
            <p>Sin conversaciones.<br>Crea una nueva.</p>
          </div>
        } @else {
          <ul class="conv-list">
            @for (c of conversations(); track c.id) {
              <li class="conv-item"
                [class.active]="activeConv()?.id === c.id"
                (click)="selectConversation(c)">
                <span class="conv-icon">⚡</span>
                <div class="conv-meta">
                  <span class="conv-title">{{ c.title }}</span>
                  <span class="conv-date">{{ c.createdAt | date:'d MMM' }}</span>
                </div>
              </li>
            }
          </ul>
        }
      </aside>

      <!-- ── Message panel ── -->
      <main class="msg-panel">

        @if (!activeConv()) {
          <div class="empty-state">
            <div class="empty-orb"></div>
            <div class="empty-content">
              <h2 class="empty-title">COACH AI</h2>
              <p class="empty-sub">Tu nutricionista y entrenador personal con IA</p>
              <ul class="empty-hints">
                <li (click)="quickStart('¿Qué desayuno recomiendas para perder peso?')">
                  "¿Qué desayuno recomiendas para perder peso?"
                </li>
                <li (click)="quickStart('¿Cuántas proteínas debo consumir hoy?')">
                  "¿Cuántas proteínas debo consumir hoy?"
                </li>
                <li (click)="quickStart('Crea un plan de entrenamiento semanal')">
                  "Crea un plan de entrenamiento semanal"
                </li>
              </ul>
              <button class="btn btn-primary btn-new-main" (click)="newConversation()">
                Iniciar conversación
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M3 7h8M7 3l4 4-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </button>
            </div>
          </div>
        } @else {
          <!-- Chat header -->
          <header class="chat-header">
            <div class="chat-header-left">
              <div class="ai-avatar">⚡</div>
              <div>
                <p class="chat-header-name">COACH AI</p>
                <p class="chat-header-status">
                  @if (sending()) {
                    <span class="status-thinking">Analizando<span class="dot-1">.</span><span class="dot-2">.</span><span class="dot-3">.</span></span>
                  } @else {
                    <span class="status-online">● En línea</span>
                  }
                </p>
              </div>
            </div>
            <span class="conv-badge">{{ activeConv()?.title }}</span>
          </header>

          <!-- Messages -->
          <div class="messages-wrap" #messagesWrap>
            @if (loadingMsgs()) {
              <div class="msgs-loading">
                <div class="loading-pill">Cargando conversación...</div>
              </div>
            } @else if (messages().length === 0) {
              <div class="msgs-empty">
                <div class="greeting-card">
                  <p class="greeting-label">COACH AI LISTO</p>
                  <p class="greeting-text">¿En qué puedo ayudarte hoy?<br>Pregúntame sobre nutrición, entrenamiento o tu progreso.</p>
                </div>
              </div>
            } @else {
              <div class="messages-inner">
                @for (msg of messages(); track msg.id) {
                  <div class="msg-row" [class.user-row]="msg.role === 'user'" [class.ai-row]="msg.role === 'assistant'">
                    @if (msg.role === 'assistant') {
                      <div class="ai-badge">⚡</div>
                    }
                    <div class="bubble" [class.user-bubble]="msg.role === 'user'" [class.ai-bubble]="msg.role === 'assistant'">
                      <p class="bubble-text">{{ msg.content }}</p>
                      <span class="bubble-ts">{{ msg.createdAt | date:'HH:mm' }}</span>
                    </div>
                  </div>
                }

                @if (sending()) {
                  <div class="msg-row ai-row">
                    <div class="ai-badge">⚡</div>
                    <div class="bubble ai-bubble typing-bubble">
                      <span class="typing-dot"></span>
                      <span class="typing-dot"></span>
                      <span class="typing-dot"></span>
                    </div>
                  </div>
                }
              </div>
            }
          </div>

          @if (sendError()) {
            <div class="send-error">{{ sendError() }}</div>
          }

          <!-- Input bar -->
          <div class="input-bar">
            <textarea
              class="chat-input"
              [(ngModel)]="inputText"
              name="chatInput"
              placeholder="Escribe tu mensaje... (Enter para enviar, Shift+Enter para nueva línea)"
              rows="1"
              (keydown.enter)="onEnter($event)"
              (input)="autoResize($event)"
              [disabled]="sending()">
            </textarea>
            <button class="send-btn" (click)="send()" [disabled]="!inputText.trim() || sending()">
              @if (sending()) {
                <span class="spinner-sm"></span>
              } @else {
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M2 8l12-6-6 12V9L2 8z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                </svg>
              }
            </button>
          </div>
        }
      </main>
    </div>
  `,
  styles: [`
    .chat-layout {
      display: flex;
      height: calc(100vh - 4rem);
      margin: -2rem;
      overflow: hidden;
    }

    /* ── Conversation sidebar ── */
    .conv-sidebar {
      width: 240px;
      min-width: 240px;
      background: #0a0a0a;
      border-right: 1px solid var(--border);
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    .conv-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 1.25rem 1rem 0.75rem;
      border-bottom: 1px solid var(--border);
      flex-shrink: 0;
    }

    .sidebar-eyebrow {
      font-size: 0.65rem;
      font-weight: 600;
      letter-spacing: 0.14em;
      text-transform: uppercase;
      color: #555;
    }

    .btn-new-conv {
      width: 28px; height: 28px;
      background: rgba(212,178,0,0.1);
      border: 1px solid rgba(212,178,0,0.25);
      border-radius: 6px;
      color: var(--accent);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: background 0.15s;
    }
    .btn-new-conv:hover { background: rgba(212,178,0,0.2); }

    .conv-list { list-style: none; overflow-y: auto; flex: 1; padding: 0.5rem 0; }

    .conv-item {
      display: flex;
      align-items: center;
      gap: 0.6rem;
      padding: 0.65rem 1rem;
      cursor: pointer;
      transition: background 0.12s;
      border-left: 2px solid transparent;
    }
    .conv-item:hover { background: rgba(255,255,255,0.03); }
    .conv-item.active { background: rgba(212,178,0,0.06); border-left-color: var(--accent); }

    .conv-icon { font-size: 0.85rem; flex-shrink: 0; opacity: 0.6; }
    .conv-meta { display: flex; flex-direction: column; min-width: 0; }
    .conv-title { font-size: 0.8rem; color: #ccc; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-weight: 500; }
    .conv-date { font-size: 0.65rem; color: #555; margin-top: 0.1rem; }

    .conv-empty { padding: 2rem 1rem; text-align: center; font-size: 0.78rem; color: #444; line-height: 1.6; }

    .conv-loading { padding: 0.75rem; display: flex; flex-direction: column; gap: 0.5rem; }
    .conv-skeleton {
      height: 40px;
      background: linear-gradient(90deg, #111 25%, #181818 50%, #111 75%);
      background-size: 200% 100%;
      border-radius: 6px;
      animation: shimmer 1.4s infinite;
    }
    @keyframes shimmer { to { background-position: -200% 0; } }

    /* ── Message panel ── */
    .msg-panel { flex: 1; display: flex; flex-direction: column; overflow: hidden; background: var(--bg); }

    /* Empty state */
    .empty-state {
      flex: 1; display: flex; align-items: center; justify-content: center;
      position: relative; overflow: hidden;
    }

    .empty-orb {
      position: absolute;
      width: 400px; height: 400px;
      border-radius: 50%;
      background: radial-gradient(circle, rgba(212,178,0,0.06) 0%, transparent 60%);
      pointer-events: none;
      animation: pulseOrb 4s ease-in-out infinite;
    }

    @keyframes pulseOrb {
      0%, 100% { transform: scale(1); opacity: 0.6; }
      50%       { transform: scale(1.1); opacity: 1; }
    }

    .empty-content {
      position: relative; text-align: center; max-width: 420px; padding: 2rem;
      animation: fadeIn 0.5s ease both;
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(12px); }
      to   { opacity: 1; transform: translateY(0); }
    }

    .empty-title {
      font-family: 'Bebas Neue', sans-serif;
      font-size: 3.5rem;
      letter-spacing: 0.1em;
      color: var(--accent);
      line-height: 1;
      margin-bottom: 0.5rem;
    }

    .empty-sub { font-size: 0.85rem; color: #666; margin-bottom: 2rem; }

    .empty-hints { list-style: none; display: flex; flex-direction: column; gap: 0.6rem; margin-bottom: 2rem; }
    .empty-hints li {
      background: var(--surface); border: 1px solid var(--border);
      border-radius: 8px; padding: 0.6rem 1rem;
      font-size: 0.8rem; color: #888; cursor: pointer;
      transition: border-color 0.15s, color 0.15s;
    }
    .empty-hints li:hover { border-color: rgba(212,178,0,0.3); color: #bbb; }

    .btn-new-main {
      padding: 0.75rem 2rem; border-radius: 10px; font-size: 0.9rem;
      transition: box-shadow 0.2s, transform 0.1s;
    }
    .btn-new-main:hover { box-shadow: 0 0 20px rgba(212,178,0,0.25); transform: translateY(-1px); }

    /* ── Chat header ── */
    .chat-header {
      display: flex; align-items: center; justify-content: space-between;
      padding: 0.9rem 1.5rem;
      border-bottom: 1px solid var(--border);
      background: rgba(10,10,10,0.5);
      backdrop-filter: blur(8px);
      flex-shrink: 0;
    }

    .chat-header-left { display: flex; align-items: center; gap: 0.75rem; }

    .ai-avatar {
      width: 36px; height: 36px;
      background: rgba(212,178,0,0.1);
      border: 1px solid rgba(212,178,0,0.25);
      border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      font-size: 0.9rem;
    }

    .chat-header-name { font-family: 'Bebas Neue', sans-serif; font-size: 1rem; letter-spacing: 0.1em; color: var(--accent); }
    .chat-header-status { font-size: 0.68rem; margin-top: 0.1rem; }
    .status-online { color: #4ade80; }
    .status-thinking { color: #d4b200; }

    .dot-1, .dot-2, .dot-3 { animation: blink 1.2s infinite; }
    .dot-2 { animation-delay: 0.2s; }
    .dot-3 { animation-delay: 0.4s; }
    @keyframes blink { 0%, 80%, 100% { opacity: 0; } 40% { opacity: 1; } }

    .conv-badge {
      font-size: 0.7rem; color: #555;
      background: var(--surface); border: 1px solid var(--border);
      border-radius: 100px; padding: 0.2rem 0.75rem;
      max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
    }

    /* ── Messages ── */
    .messages-wrap { flex: 1; overflow-y: auto; padding: 1.5rem; scroll-behavior: smooth; }
    .messages-inner { display: flex; flex-direction: column; gap: 1.25rem; }

    .msg-row {
      display: flex; align-items: flex-end; gap: 0.6rem;
      animation: msgIn 0.25s ease both;
    }
    @keyframes msgIn {
      from { opacity: 0; transform: translateY(8px); }
      to   { opacity: 1; transform: translateY(0); }
    }

    .user-row { flex-direction: row-reverse; }

    .ai-badge {
      width: 28px; height: 28px;
      background: rgba(212,178,0,0.08);
      border: 1px solid rgba(212,178,0,0.15);
      border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      font-size: 0.75rem; flex-shrink: 0;
    }

    .bubble { max-width: 68%; padding: 0.75rem 1rem; border-radius: 16px; position: relative; }

    .ai-bubble {
      background: #111; border: 1px solid #1e1e1e;
      border-left: 3px solid rgba(212,178,0,0.4);
      border-radius: 4px 16px 16px 16px;
    }

    .user-bubble {
      background: rgba(212,178,0,0.1);
      border: 1px solid rgba(212,178,0,0.2);
      border-radius: 16px 4px 16px 16px;
    }

    .bubble-text { font-size: 0.875rem; line-height: 1.6; color: var(--text); white-space: pre-wrap; word-break: break-word; }
    .bubble-ts { display: block; font-size: 0.62rem; color: #444; margin-top: 0.4rem; text-align: right; }

    /* Typing indicator */
    .typing-bubble { display: flex; gap: 4px; align-items: center; padding: 0.85rem 1rem; }
    .typing-dot {
      width: 6px; height: 6px; border-radius: 50%;
      background: rgba(212,178,0,0.5);
      animation: typingBounce 1.2s infinite ease;
    }
    .typing-dot:nth-child(2) { animation-delay: 0.2s; }
    .typing-dot:nth-child(3) { animation-delay: 0.4s; }
    @keyframes typingBounce {
      0%, 80%, 100% { transform: translateY(0); opacity: 0.4; }
      40%            { transform: translateY(-5px); opacity: 1; }
    }

    /* Loading / empty */
    .msgs-loading { display: flex; align-items: center; justify-content: center; height: 100%; }
    .loading-pill {
      background: var(--surface); border: 1px solid var(--border);
      border-radius: 100px; padding: 0.45rem 1.25rem;
      font-size: 0.78rem; color: #666;
    }
    .msgs-empty { display: flex; align-items: center; justify-content: center; height: 100%; }
    .greeting-card { text-align: center; padding: 2rem; }
    .greeting-label { font-size: 0.65rem; font-weight: 600; letter-spacing: 0.18em; text-transform: uppercase; color: var(--accent); margin-bottom: 0.75rem; }
    .greeting-text { font-size: 0.9rem; color: #666; line-height: 1.7; }

    /* Error */
    .send-error {
      margin: 0 1.5rem 0.5rem; font-size: 0.78rem; color: #e05252;
      background: rgba(224,82,82,0.08); border: 1px solid rgba(224,82,82,0.2);
      border-radius: 8px; padding: 0.5rem 0.75rem;
    }

    /* ── Input bar ── */
    .input-bar {
      display: flex; align-items: flex-end; gap: 0.75rem;
      padding: 1rem 1.5rem 1.25rem;
      border-top: 1px solid var(--border);
      background: rgba(10,10,10,0.5);
      backdrop-filter: blur(8px);
      flex-shrink: 0;
    }

    .chat-input {
      flex: 1;
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 12px;
      color: var(--text);
      font-family: 'DM Sans', sans-serif;
      font-size: 0.875rem;
      line-height: 1.5;
      padding: 0.7rem 1rem;
      resize: none;
      outline: none;
      transition: border-color 0.15s, box-shadow 0.15s;
      max-height: 140px;
      overflow-y: auto;
    }
    .chat-input:focus { border-color: var(--accent); box-shadow: 0 0 0 2px rgba(212,178,0,0.1); }
    .chat-input::placeholder { color: #444; }
    .chat-input:disabled { opacity: 0.5; }

    .send-btn {
      width: 42px; height: 42px;
      background: var(--accent); border: none; border-radius: 50%;
      color: #000; cursor: pointer;
      display: flex; align-items: center; justify-content: center;
      flex-shrink: 0;
      transition: background 0.15s, transform 0.1s, box-shadow 0.2s;
    }
    .send-btn:hover:not(:disabled) { background: var(--accent-hover); box-shadow: 0 0 16px rgba(212,178,0,0.4); transform: scale(1.05); }
    .send-btn:active:not(:disabled) { transform: scale(0.97); }
    .send-btn:disabled { opacity: 0.35; cursor: not-allowed; }

    .spinner-sm {
      width: 14px; height: 14px;
      border: 2px solid rgba(0,0,0,0.2);
      border-top-color: #000;
      border-radius: 50%;
      animation: spin 0.7s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    @media (max-width: 640px) {
      .conv-sidebar { width: 60px; min-width: 60px; }
      .conv-meta, .sidebar-eyebrow, .conv-date, .conv-badge { display: none; }
    }
  `],
})
export class ChatComponent implements OnInit, AfterViewChecked {
  private svc = inject(ChatService);

  @ViewChild('messagesWrap') messagesWrap!: ElementRef<HTMLDivElement>;

  conversations  = signal<Conversation[]>([]);
  activeConv     = signal<Conversation | null>(null);
  messages       = signal<ChatMessage[]>([]);
  loadingConvs   = signal(true);
  loadingMsgs    = signal(false);
  sending        = signal(false);
  sendError      = signal('');

  inputText = '';
  private shouldScroll = false;

  ngOnInit() {
    this.svc.getConversations().subscribe({
      next: convs => { this.conversations.set(convs); this.loadingConvs.set(false); },
      error: () => this.loadingConvs.set(false),
    });
  }

  ngAfterViewChecked() {
    if (this.shouldScroll && this.messagesWrap) {
      const el = this.messagesWrap.nativeElement;
      el.scrollTop = el.scrollHeight;
      this.shouldScroll = false;
    }
  }

  selectConversation(conv: Conversation) {
    if (this.activeConv()?.id === conv.id) return;
    this.activeConv.set(conv);
    this.messages.set([]);
    this.sendError.set('');
    this.loadingMsgs.set(true);
    this.svc.getMessages(conv.id).subscribe({
      next: msgs => { this.messages.set(msgs); this.loadingMsgs.set(false); this.shouldScroll = true; },
      error: () => this.loadingMsgs.set(false),
    });
  }

  newConversation() {
    this.svc.createConversation().subscribe({
      next: conv => {
        this.conversations.update(list => [conv, ...list]);
        this.selectConversation(conv);
      },
      error: () => {},
    });
  }

  quickStart(prompt: string) {
    this.svc.createConversation().subscribe({
      next: conv => {
        this.conversations.update(list => [conv, ...list]);
        this.activeConv.set(conv);
        this.messages.set([]);
        this.inputText = prompt;
        this.send();
      },
      error: () => {},
    });
  }

  send() {
    const content = this.inputText.trim();
    if (!content || !this.activeConv() || this.sending()) return;

    const optimistic: ChatMessage = {
      id: Date.now(),
      role: 'user',
      content,
      createdAt: new Date().toISOString(),
    };

    this.messages.update(m => [...m, optimistic]);
    this.inputText = '';
    this.sending.set(true);
    this.sendError.set('');
    this.shouldScroll = true;

    this.svc.sendMessage(this.activeConv()!.id, content).subscribe({
      next: reply => {
        this.messages.update(m => [...m, reply]);
        this.sending.set(false);
        this.shouldScroll = true;
      },
      error: err => {
        this.sendError.set(
          err.status === 429
            ? 'Límite de mensajes alcanzado (20/hora). Espera un momento.'
            : 'Error al enviar. Intenta de nuevo.'
        );
        this.sending.set(false);
      },
    });
  }

  onEnter(e: Event) {
    const ke = e as KeyboardEvent;
    if (!ke.shiftKey) {
      ke.preventDefault();
      this.send();
    }
  }

  autoResize(e: Event) {
    const el = e.target as HTMLTextAreaElement;
    el.style.height = 'auto';
    el.style.height = Math.min(el.scrollHeight, 140) + 'px';
  }
}
