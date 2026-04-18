import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface Conversation { id: number; title: string; createdAt: string; }
export interface ChatMessage   { id: number; role: 'user' | 'assistant'; content: string; createdAt: string; }

@Injectable({ providedIn: 'root' })
export class ChatService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/chat`;

  getConversations() {
    return this.http.get<Conversation[]>(`${this.base}/conversations`);
  }

  createConversation() {
    return this.http.post<Conversation>(`${this.base}/conversations`, {});
  }

  getMessages(conversationId: number) {
    return this.http.get<ChatMessage[]>(`${this.base}/conversations/${conversationId}/messages`);
  }

  sendMessage(conversationId: number, content: string) {
    return this.http.post<ChatMessage>(`${this.base}/conversations/${conversationId}/messages`, { content });
  }
}
