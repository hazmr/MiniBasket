import Link from "next/link";
import { register } from "@/actions/auth";
import { ActionForm } from "@/components/action-form";
import { AuthCard } from "@/components/auth-card";
import { SubmitButton } from "@/components/submit-button";
import { Field, FieldDescription, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";

export default function RegisterPage() {
  return (
    <AuthCard
      title="Create an account"
      description="It takes less than a minute."
      footer={<p>Already have an account? <Link href="/login" className="font-medium text-primary hover:underline">Log in</Link></p>}
    >
      <ActionForm action={register} className="flex flex-col gap-5">
        <FieldGroup className="gap-4">
          <Field>
            <FieldLabel htmlFor="email">Email</FieldLabel>
            <Input id="email" type="email" name="email" required autoComplete="email" placeholder="you@example.com" />
          </Field>
          <Field>
            <FieldLabel htmlFor="password">Password</FieldLabel>
            <Input id="password" type="password" name="password" required minLength={8} maxLength={72} autoComplete="new-password" />
            <FieldDescription>At least 8 characters.</FieldDescription>
          </Field>
        </FieldGroup>
        <SubmitButton size="lg" className="w-full">Sign up</SubmitButton>
      </ActionForm>
    </AuthCard>
  );
}
