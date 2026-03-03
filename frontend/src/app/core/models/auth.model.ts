export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    name: string;
    email: string;
    password: string;
}


export interface AuthResponse {
    token: string;
    tokenType: string;
    expiresIn: number;
    userId: string;
    email: string;
    name: string;
}


export interface UserProfile {
    id: string;
    name: string;
    email: string;
}
