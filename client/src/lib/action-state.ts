export type ActionState = { error?: string; message?: string; href?: string; hrefLabel?: string };

export type FormAction = (state: ActionState, formData: FormData) => Promise<ActionState>;
